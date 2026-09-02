package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.theme.BackgroundTint
import kotlinx.coroutines.delay

/** Roughly the length of the greeting, plus a beat to look at her. */
private const val INTRO_MS = 8000L

/**
 * Պույ-պույ waves and introduces herself.
 *
 * Tapping anywhere skips ahead — after the tenth time, waiting through the greeting is the
 * last thing anybody wants.
 */
@Composable
fun IntroScreen(sounds: SoundBank, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val entrance = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        entrance.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        sounds.play("intro")
        delay(INTRO_MS)
        onDone()
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(BackgroundTint.Paper.color)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDone,
            )
    ) {
        val mouseSize = minOf(maxWidth, maxHeight) * 0.62f
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            PouyPouy(
                state = HelperState.Waving,
                size = mouseSize,
                modifier = Modifier.scale(entrance.value),
            )
        }
    }
}
