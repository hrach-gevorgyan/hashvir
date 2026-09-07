package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.audio.rememberSpeaking
import com.hrach.hashvir.theme.BackgroundTint
import kotlinx.coroutines.delay

/** Roughly the length of the greeting, plus a beat to look at him. */
private const val INTRO_MS = 7600L

/** How long to wait for the greeting to finish decoding before giving up on it. */
private const val READY_TIMEOUT_MS = 4000L

/**
 * Պույ-պույ waves and introduces himself.
 *
 * Tapping anywhere skips ahead — after the tenth time, waiting through the greeting is the
 * last thing anybody wants.
 */
@Composable
fun IntroScreen(sounds: SoundBank, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val entrance = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        entrance.animateTo(1f, tween(400, easing = FastOutSlowInEasing))

        // The greeting is the longest clip and the first thing asked for, so on a slower
        // device it can still be decoding here. Waiting for it keeps him from waving in
        // silence; if it never arrives the screen still moves on.
        var waited = 0L
        while (!sounds.isReady("intro") && waited < READY_TIMEOUT_MS) {
            delay(50)
            waited += 50
        }

        sounds.play("intro")
        delay(INTRO_MS)
        sounds.stopAll()
        onDone()
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(BackgroundTint.Sand.color)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                // Tapping through the greeting has to silence it, or he talks over the menu.
                sounds.stopAll()
                onDone()
            }
    ) {
        val mouseSize = minOf(maxWidth, maxHeight) * 0.52f

        Scenery(seed = 3)

        Beach(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(88.dp),
        )

        // He is standing on the beach with the coconut he is about to chase.
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = maxHeight * 0.10f),
            contentAlignment = Alignment.BottomCenter,
        ) {
            PouyPouy(
                state = HelperState.Waving,
                size = mouseSize,
                modifier = Modifier.scale(entrance.value),
                speaking = rememberSpeaking(sounds),
            )
        }

        Canvas(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = maxHeight * 0.085f, start = mouseSize)
                .size(mouseSize * 0.30f)
                .scale(entrance.value)
        ) {
            drawCoconut(size.minDimension)
        }
    }
}
