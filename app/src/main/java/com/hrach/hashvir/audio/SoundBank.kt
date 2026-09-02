package com.hrach.hashvir.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

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

    /** Sound ids SoundPool has finished decoding. Playing before this is silent. */
    private val loaded = mutableSetOf<Int>()

    private var muted = false
    private var volume = 1f

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loaded += sampleId else Log.w(TAG, "decode failed for id $sampleId")
        }
        val resources = context.resources
        for (name in ALL) {
            @Suppress("DiscouragedApi")
            val resId = resources.getIdentifier(name, "raw", context.packageName)
            if (resId == 0) {
                Log.w(TAG, "missing clip: $name")
                continue
            }
            sounds[name] = soundPool.load(context, resId, 1)
        }
        Log.i(TAG, "preloaded ${sounds.size} of ${ALL.size} clips")
    }

    fun play(name: String) {
        if (muted) return
        val id = sounds[name]
        if (id == null) {
            Log.w(TAG, "play($name): not loaded")
            return
        }
        if (id !in loaded) {
            Log.w(TAG, "play($name): still decoding")
            return
        }
        soundPool.play(id, volume, volume, 1, 0, 1f)
    }

    fun setVolume(value: Float) {
        volume = value.coerceIn(0f, 1f)
    }

    fun setMuted(value: Boolean) {
        muted = value
    }

    fun release() {
        soundPool.release()
        sounds.clear()
        loaded.clear()
    }

    companion object {
        /** Every clip the game can ask for. See AUDIO.md. */
        val ALL: List<String> = buildList {
            for (n in 1..10) add("num_$n")
            for (n in 1..10) add("total_$n")
            for (n in 1..10) add("ask_$n")
            for (n in 1..4) add("praise_$n")
            add("oops_1")
            add("oops_2")
            add("chime")
            for (n in 1..3) add("star_$n")
        }
    }
}
