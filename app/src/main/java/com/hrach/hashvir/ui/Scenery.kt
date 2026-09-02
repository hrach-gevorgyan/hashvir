package com.hrach.hashvir.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.random.Random

/**
 * Muted background texture: soft blobs and scattered dots, all at very low alpha.
 *
 * The point is to stop the pastel grounds reading as blank paper without ever competing with
 * the fruit or the numerals. Nothing here moves, and nothing here is anywhere near as
 * saturated as the thing she is meant to be looking at.
 */
@Composable
fun Scenery(seed: Int, tint: Color, modifier: Modifier = Modifier) {
    val blobs = remember(seed) {
        val random = Random(seed)
        List(7) {
            Triple(
                Offset(random.nextFloat(), random.nextFloat()),
                0.10f + random.nextFloat() * 0.16f,
                random.nextFloat(),
            )
        }
    }
    val dots = remember(seed) {
        val random = Random(seed + 1)
        List(26) { Offset(random.nextFloat(), random.nextFloat()) to random.nextFloat() }
    }

    Canvas(modifier.fillMaxSize()) {
        val shorter = size.minDimension
        for ((centre, radius, shade) in blobs) {
            drawCircle(
                color = tint.copy(alpha = 0.05f + shade * 0.04f),
                radius = shorter * radius,
                center = Offset(centre.x * size.width, centre.y * size.height),
            )
        }
        for ((point, shade) in dots) {
            drawCircle(
                color = tint.copy(alpha = 0.10f + shade * 0.08f),
                radius = shorter * (0.006f + shade * 0.010f),
                center = Offset(point.x * size.width, point.y * size.height),
            )
        }
    }
}

/**
 * A grassy strip along the bottom for Պույ-պույ to walk on, so she is standing on something
 * rather than floating over the content.
 */
@Composable
fun GroundStrip(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth()) {
        val top = size.height * 0.55f
        val hill = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, top + size.height * 0.16f)
            cubicTo(
                size.width * 0.30f, top - size.height * 0.14f,
                size.width * 0.70f, top + size.height * 0.24f,
                size.width, top - size.height * 0.04f,
            )
            lineTo(size.width, size.height)
            close()
        }
        drawPath(hill, tint.copy(alpha = 0.22f))
        // A few blades, spaced unevenly so it does not read as a pattern.
        for (fraction in listOf(0.08f, 0.17f, 0.29f, 0.44f, 0.58f, 0.71f, 0.83f, 0.94f)) {
            val x = size.width * fraction
            drawLine(
                color = tint.copy(alpha = 0.32f),
                start = Offset(x, size.height * 0.92f),
                end = Offset(x + size.height * 0.06f, size.height * 0.62f),
                strokeWidth = size.height * 0.05f,
                cap = StrokeCap.Round,
            )
        }
    }
}

/**
 * Պույ-պույ strolling back and forth along the bottom of the screen, turning around at each
 * end rather than sliding backwards.
 */
@Composable
fun WalkingHelper(height: Dp, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "stroll")
    val progress by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(11000, easing = LinearEasing), RepeatMode.Restart),
        label = "stroll",
    )

    BoxWithConstraints(modifier.fillMaxWidth().height(height)) {
        // A triangle wave: right for the first half, left for the second.
        val sweep = 1f - abs(progress * 2f - 1f)
        val facingLeft = progress > 0.5f
        val travel = maxWidth - height

        Box(
            Modifier
                .align(Alignment.BottomStart)
                .graphicsLayer {
                    translationX = travel.toPx() * sweep
                    // Mirror her instead of walking backwards.
                    scaleX = if (facingLeft) -1f else 1f
                }
        ) {
            PouyPouy(state = HelperState.Walking, size = height)
        }
    }
}
