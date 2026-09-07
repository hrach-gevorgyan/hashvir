package com.hrach.hashvir.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

private const val TAG = "SoundBank"

/**
 * SoundPool rather than MediaPlayer: the clips are short and several overlap — a tap sound
 * can land while the total is still playing.
 *
 * Everything is preloaded at startup so a tap never waits on disk. A clip that is missing
 * from res/raw logs once and then no-ops; it never crashes and never blocks the count.
 */
class SoundBank(context: Context) {

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    /** Clip name -> SoundPool sound id. Absent means the file is not in res/raw. */
    private val sounds = mutableMapOf<String, Int>()

    /** Streams currently playing, so a long clip can be cut short when a screen is left. */
    private val playing = mutableListOf<Int>()

    /**
     * Sound ids SoundPool has finished decoding. Playing before this is silent.
     *
     * Written from SoundPool's own callback thread and read from the main thread, so it has to
     * be concurrent.
     */
    private val loaded: MutableSet<Int> = ConcurrentHashMap.newKeySet()


    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loaded += sampleId else Log.w(TAG, "decode failed for id $sampleId")
        }
        val resources = context.resources
        for (name in ALL) {
            @Suppress("DiscouragedApi")
            val resId = resources.getIdentifier(name, "raw", context.packageName)
            if (resId == 0) {
                // Praise slots are meant to be optional; anything else missing is a mistake.
                if (!name.startsWith("praise_")) Log.w(TAG, "missing clip: $name")
                continue
            }
            sounds[name] = soundPool.load(context, resId, 1)
        }
        Log.i(TAG, "preloaded ${sounds.size} of ${ALL.size} clips")
    }

    /**
     * When the current speech clip finishes, as device uptime.
     *
     * Every word the child hears is Պույ-պույ's, so his mouth has to move while one is
     * playing. SoundPool cannot report progress, so the length is taken from the measured
     * table below and the clock does the rest.
     */
    var speakingUntil by mutableLongStateOf(0L)
        private set

    fun play(name: String) {
        val id = sounds[name]
        if (id == null) {
            Log.w(TAG, "play($name): not loaded")
            return
        }
        if (id !in loaded) {
            Log.w(TAG, "play($name): still decoding")
            return
        }
        val stream = soundPool.play(id, 1f, 1f, 1, 0, 1f)
        if (stream != 0) {
            playing += stream
            if (playing.size > 8) playing.removeAt(0)
        }

        val spoken = speechLengthMs(name)
        if (spoken > 0) {
            speakingUntil = SystemClock.uptimeMillis() + spoken
        }
    }

    /** Zero for the chime and the star notes: those are sounds, not speech. */
    private fun speechLengthMs(name: String): Long = when {
        name == "intro" -> 6850
        name == "what_number" -> 1250
        name == "how_many" -> 1900
        name.startsWith("total_") -> 1470
        name.startsWith("ask_") -> 1160
        name.startsWith("praise_") -> 1350
        name.startsWith("num_") -> 860
        name.startsWith("oops_") -> 810
        else -> 0
    }

    /** Praise, in shuffled order. */
    fun playPraise() = playFromDeck("praise_")

    /** The soft "not yet", alternating rather than landing on the same one repeatedly. */
    fun playOops() = playFromDeck("oops_")

    /**
     * Plays one clip of a group, dealing from a shuffled deck rather than drawing at random.
     *
     * Independent random draws repeat far more than people expect: with eight clips the same
     * one comes up twice running about one time in eight, and three times running often enough
     * to be noticed within a session. A deck guarantees every other clip is heard before any
     * one repeats, and a fresh deck never opens with the clip that closed the last.
     */
    private fun playFromDeck(prefix: String) {
        val available = sounds.keys.filter { it.startsWith(prefix) && isReady(it) }
        if (available.isEmpty()) return

        val deck = decks.getOrPut(prefix) { mutableListOf() }
        if (deck.isEmpty()) {
            deck += available.shuffled()
            if (deck.size > 1 && deck.first() == lastOfGroup[prefix]) {
                deck += deck.removeAt(0)
            }
        }

        val pick = deck.removeAt(0)
        lastOfGroup[prefix] = pick
        play(pick)
    }

    private val decks = mutableMapOf<String, MutableList<String>>()
    private val lastOfGroup = mutableMapOf<String, String>()

    /**
     * True once [name] has finished decoding and will actually be audible.
     *
     * The intro is by far the longest clip and is asked for barely a moment after launch, so on
     * a slower device it was still decoding and played silently. Callers with fixed timing
     * should wait for this rather than assuming a clip is ready.
     */
    fun isReady(name: String): Boolean = sounds[name]?.let { it in loaded } == true

    /** Cuts off whatever is playing — skipping the intro has to stop the intro. */
    fun stopAll() {
        for (stream in playing) soundPool.stop(stream)
        playing.clear()
        speakingUntil = 0L
    }

    fun release() {
        soundPool.release()
        sounds.clear()
        loaded.clear()
    }

    companion object {
        /**
         * How many praise clips the app will look for. Only the ones that exist are ever
         * played, so recording another one is a matter of dropping praise_9.ogg into res/raw
         * — no code change, no list to update.
         */
        const val PRAISE_SLOTS = 12

        /** Every clip the game can ask for. See AUDIO.md. */
        val ALL: List<String> = buildList {
            for (n in 1..10) add("num_$n")
            for (n in 1..10) add("total_$n")
            for (n in 1..10) add("ask_$n")
            for (n in 1..PRAISE_SLOTS) add("praise_$n")
            add("oops_1")
            add("oops_2")
            add("chime")
            for (n in 1..3) add("star_$n")
            add("intro")
            add("what_number")
            add("how_many")
        }
    }
}

/**
 * True while a speech clip is still playing, recomputed every frame so Պույ-պույ's mouth can
 * follow it.
 */
@Composable
fun rememberSpeaking(sounds: SoundBank): Boolean {
    var speaking by remember { mutableStateOf(false) }
    // Keyed on the end time, so the loop only runs while a clip is actually playing and
    // stops the moment it finishes rather than polling for the life of the screen.
    LaunchedEffect(sounds.speakingUntil) {
        while (SystemClock.uptimeMillis() < sounds.speakingUntil) {
            speaking = true
            withFrameNanos { }
        }
        speaking = false
    }
    return speaking
}
