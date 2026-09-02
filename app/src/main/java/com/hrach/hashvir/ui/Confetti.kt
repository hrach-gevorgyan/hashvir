package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.hrach.hashvir.theme.Feedback
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val PARTICLES = 18
private const val DURATION_MS = 400

/** A short burst from the correct card. Green, never red — there are no errors in this app. */
@Composable
fun Confetti(origin: Offset, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    val seeds = remember(origin) {
        val random = Random(origin.hashCode())
        List(PARTICLES) {
            Triple(
                random.nextFloat() * 2f * PI.toFloat(),
                0.6f + random.nextFloat() * 0.8f,
                4f + random.nextFloat() * 5f,
            )
        }
    }

    LaunchedEffect(origin) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(DURATION_MS, easing = FastOutSlowInEasing))
    }

    Canvas(modifier.fillMaxSize()) {
        val t = progress.value
        if (t == 0f || t == 1f) return@Canvas
        val reach = size.minDimension * 0.28f

        for ((angle, speed, radius) in seeds) {
            val distance = reach * speed * t
            val x = origin.x + cos(angle) * distance
            // A little gravity, so it falls rather than floating outward forever.
            val y = origin.y + sin(angle) * distance + reach * 0.5f * t * t
            drawRect(
                color = Feedback.Positive.copy(alpha = 1f - t),
                topLeft = Offset(x - radius / 2f, y - radius / 2f),
                size = Size(radius, radius),
            )
        }
    }
}
