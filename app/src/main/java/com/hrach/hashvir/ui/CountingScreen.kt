package com.hrach.hashvir.ui

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.hrach.hashvir.BuildConfig
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.audio.rememberSpeaking
import com.hrach.hashvir.game.Layout
import com.hrach.hashvir.game.Round
import kotlin.random.Random
import kotlinx.coroutines.delay

/** A second after the last tap: the fruit gather, and only then is she asked. */
private const val GATHER_DELAY_MS = 1000L

/**
 * How long she gets to answer «Քանի՞ հատ էր» before Պույ-պույ confirms it.
 *
 * This is the cardinality step: counting only becomes quantity once the child produces the
 * last number herself as the answer to how many. The fruit stay on screen, gathered into one
 * group, for the whole of it.
 *
 * QUESTION_MS covers the clip; THINKING_MS is the four seconds of silence after it, which
 * are hers. Long enough to get the word out without being rushed.
 */
private const val QUESTION_MS = 1900L
private const val THINKING_MS = 4000L

/**
 * Round-end audio runs strictly one clip at a time. Measured after normalization: total up to
 * 1.56s, praise 1.09s, chime 0.86s.
 */
private const val AFTER_TOTAL_MS = 1750L
private const val AFTER_PRAISE_MS = 1250L
private const val AFTER_CHIME_MS = 1100L

private enum class Phase { Counting, Asking, Answered }

@Composable
fun CountingScreen(
    round: Round,
    sounds: SoundBank,
    compact: Boolean,
    onTap: (Int) -> Unit,
    onRoundFinished: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keyed on the round's identity, not the Round instance: tapping copies the round, and
    // remember(round) would reset the phase on every tap. Neither count nor fruit repeats
    // consecutively, so this key always changes between rounds.
    var phase by remember(round.count, round.fruit) { mutableStateOf(Phase.Counting) }

    LaunchedEffect(round.isComplete) {
        if (!round.isComplete) return@LaunchedEffect

        delay(GATHER_DELAY_MS)
        phase = Phase.Asking
        // "How many were there?" — she answers out loud, to herself and to whoever is nearby.
        sounds.play("how_many")
        delay(QUESTION_MS + THINKING_MS)

        // Then, and only then, the numeral confirms what she said.
        phase = Phase.Answered
        sounds.play("total_${round.count}")
        delay(AFTER_TOTAL_MS)
        sounds.playPraise()
        delay(AFTER_PRAISE_MS)
        sounds.play("chime")
        delay(AFTER_CHIME_MS)
        onRoundFinished()
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(round.background.color)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val shorter = minOf(maxWidth, maxHeight)
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        // Fruit live above Պույ-պույ, never behind him.
        val playHeight = Layout.playHeight(maxHeight)
        val diameter = Layout.diameter(round.count, maxWidth, playHeight, compact)

        if (BuildConfig.DEBUG && diameter < Layout.PhoneFloor) {
            Log.w(
                "Layout",
                "count=${round.count} target ${diameter.value.toInt()}dp is below the " +
                    "${Layout.PhoneFloor.value.toInt()}dp floor in ${maxWidth.value.toInt()}x" +
                    "${maxHeight.value.toInt()}dp",
            )
        }

        Scenery(seed = round.count * 31 + round.fruit.ordinal)

        // Once counted, the fruit shrink and slide into one tidy group near the top: the set
        // she is being asked about, seen as a whole.
        val gatheredDiameter = diameter * 0.62f
        val gathered = remember(round.count, screenWidth, playHeight, gatheredDiameter) {
            Layout.gathered(round.count, screenWidth, playHeight, gatheredDiameter, centreY = 0.30f)
        }
        val pull by animateFloatAsState(
            targetValue = if (phase == Phase.Counting) 0f else 1f,
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            label = "gather",
        )

        for ((index, scattered) in round.positions.withIndex()) {
            val target = gathered[index]
            val x = scattered.x + (target.x - scattered.x) * pull
            val y = scattered.y + (target.y - scattered.y) * pull
            val size = diameter + (gatheredDiameter - diameter) * pull

            FruitSprite(
                fruit = round.fruit,
                diameter = size,
                tapped = index in round.tapped,
                // Gathered fruit are the answer to the question, so they come back to full
                // strength rather than staying dimmed.
                dimWhenTapped = phase == Phase.Counting,
                onTap = {
                    if (index !in round.tapped) {
                        // The number she hears is the one she is on, not a running total.
                        sounds.play("num_${round.tapped.size + 1}")
                        onTap(index)
                    }
                },
                modifier = Modifier.offset(
                    x = maxWidth * x - size / 2,
                    y = playHeight * y - size / 2,
                ),
            )
        }

        if (phase == Phase.Answered) {
            Confetti(
                origin = with(LocalDensity.current) {
                    Offset((screenWidth / 2f).toPx(), (screenHeight * 0.66f).toPx())
                }
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = screenHeight * 0.05f, start = shorter * 0.22f),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(shorter * 0.04f),
                ) {
                    Stars(sounds = sounds, starSize = shorter * 0.13f)
                    NumberGlyph(count = round.count, glyphHeight = shorter * 0.34f)
                }
            }
        }

        PouyPouy(
            state = when (phase) {
                // He waits with the child while the answer is being worked out.
                Phase.Asking -> HelperState.Thinking
                Phase.Answered -> HelperState.Happy
                Phase.Counting -> if (round.tapped.isEmpty()) HelperState.Idle else HelperState.Suggesting
            },
            speaking = rememberSpeaking(sounds),
            size = maxHeight * if (phase == Phase.Counting) 0.12f else 0.17f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(shorter * 0.03f),
        )

        BackButton(
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
        )
    }
}
