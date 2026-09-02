package com.hrach.hashvir.ui

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.hrach.hashvir.theme.Island
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * Պույ-պույ's island, drawn behind everything.
 *
 * From the tale: she finds a coconut, climbs inside, eats until she is too round to get back
 * out, and cries herself thin enough to escape. The whole app lives on that beach.
 *
 * All of it is muted and motionless. Scenery is never the subject — the fruit and the numerals
 * stay the most saturated things on screen.
 */
@Composable
fun Scenery(seed: Int, modifier: Modifier = Modifier, strength: Float = 1f) {
    val clouds = remember(seed) {
        val random = Random(seed)
        List(3) {
            Triple(
                random.nextFloat() * 0.8f,
                0.06f + random.nextFloat() * 0.22f,
                0.7f + random.nextFloat() * 0.6f,
            )
        }
    }
    val shells = remember(seed) {
        val random = Random(seed + 7)
        List(9) { Offset(random.nextFloat(), 0.55f + random.nextFloat() * 0.42f) to random.nextFloat() }
    }

    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        // Screens that are about one large numeral turn the island down rather than off.
        fun a(value: Float) = value * strength

        // Sun, top corner, barely there.
        drawCircle(Island.Sun.copy(alpha = a(0.30f)), radius = w * 0.17f, center = Offset(w * 0.86f, h * 0.07f))
        drawCircle(Island.Sun.copy(alpha = a(0.18f)), radius = w * 0.25f, center = Offset(w * 0.86f, h * 0.07f))

        for ((x, y, scale) in clouds) {
            val cx = x * w
            val cy = y * h
            val r = w * 0.07f * scale
            for ((dx, dy, rs) in listOf(Triple(-0.9f, 0.15f, 0.7f), Triple(0f, 0f, 1f), Triple(0.95f, 0.2f, 0.75f))) {
                drawCircle(
                    color = Color.White.copy(alpha = a(0.42f)),
                    radius = r * rs,
                    center = Offset(cx + dx * r, cy + dy * r),
                )
            }
        }

        // Sea band across the middle distance, with a lighter shoreline under it.
        val seaTop = h * 0.46f
        drawRect(
            color = Island.Sea.copy(alpha = a(0.16f)),
            topLeft = Offset(0f, seaTop),
            size = Size(w, h * 0.12f),
        )
        for (row in 0 until 3) {
            val y = seaTop + h * 0.03f + row * h * 0.035f
            var x = -w * 0.05f + row * w * 0.06f
            while (x < w) {
                val wave = Path().apply {
                    moveTo(x, y)
                    quadraticTo(x + w * 0.03f, y - h * 0.010f, x + w * 0.06f, y)
                }
                drawPath(wave, Island.SeaDeep.copy(alpha = a(0.22f)), style = Stroke(width = h * 0.0035f))
                x += w * 0.13f
            }
        }

        // Palm on each side, leaning in from the edges.
        palm(Offset(w * 0.03f, h * 0.62f), w * 0.30f, lean = -1f, strength = strength)
        palm(Offset(w * 0.97f, h * 0.56f), w * 0.26f, lean = 1f, strength = strength)

        // Shells and pebbles scattered on the sand.
        for ((point, shade) in shells) {
            drawOval(
                color = Island.SandDark.copy(alpha = a(0.30f + shade * 0.16f)),
                topLeft = Offset(point.x * w, point.y * h),
                size = Size(w * (0.014f + shade * 0.018f), w * (0.009f + shade * 0.010f)),
            )
        }
    }
}

/** One palm: a curved trunk with fronds fanning off the top, and a coconut or two. */
private fun DrawScope.palm(base: Offset, height: Float, lean: Float, strength: Float) {
    val topX = base.x + lean * height * 0.26f
    val topY = base.y - height

    val trunk = Path().apply {
        moveTo(base.x - height * 0.045f, base.y)
        cubicTo(
            base.x + lean * height * 0.02f, base.y - height * 0.5f,
            topX - lean * height * 0.06f, topY + height * 0.3f,
            topX, topY,
        )
        lineTo(topX + lean * height * 0.05f, topY + height * 0.03f)
        cubicTo(
            topX + height * 0.02f * lean, topY + height * 0.32f,
            base.x + lean * height * 0.06f, base.y - height * 0.5f,
            base.x + height * 0.045f, base.y,
        )
        close()
    }
    drawPath(trunk, Island.Trunk.copy(alpha = 0.30f * strength))

    for (angle in listOf(-115f, -70f, -25f, 20f, 65f)) {
        rotate(degrees = angle * 1f, pivot = Offset(topX, topY)) {
            val frond = Path().apply {
                moveTo(topX, topY)
                quadraticTo(
                    topX + height * 0.28f, topY - height * 0.16f,
                    topX + height * 0.52f, topY - height * 0.04f,
                )
                quadraticTo(
                    topX + height * 0.28f, topY + height * 0.06f,
                    topX, topY,
                )
                close()
            }
            drawPath(frond, Island.PalmLeaf.copy(alpha = 0.34f * strength))
            drawLine(
                color = Island.PalmDark.copy(alpha = 0.26f * strength),
                start = Offset(topX, topY),
                end = Offset(topX + height * 0.50f, topY - height * 0.05f),
                strokeWidth = height * 0.016f,
                cap = StrokeCap.Round,
            )
        }
    }

    drawCircle(Island.CoconutShell.copy(alpha = 0.36f * strength), radius = height * 0.055f, center = Offset(topX - height * 0.05f, topY + height * 0.07f))
    drawCircle(Island.CoconutShell.copy(alpha = 0.30f * strength), radius = height * 0.048f, center = Offset(topX + height * 0.06f, topY + height * 0.09f))
}

/** The coconut of the story: a hairy brown shell with the three dark eyes it really has. */
fun DrawScope.drawCoconut(diameter: Float, topLeft: Offset = Offset.Zero) {
    val r = diameter / 2f
    translate(topLeft.x, topLeft.y) {
        drawCircle(Island.CoconutShell, radius = r, center = Offset(r, r))
        drawCircle(Island.CoconutDark, radius = r, center = Offset(r, r), style = Stroke(width = diameter * 0.05f))
        // Fibre, in short strokes following the curve.
        for (index in 0 until 7) {
            val t = index / 6f
            val x = r * 0.45f + t * r * 1.1f
            drawLine(
                color = Island.CoconutDark.copy(alpha = 0.35f),
                start = Offset(x, r * 0.5f + sin(t * 3f) * r * 0.18f),
                end = Offset(x - diameter * 0.03f, r * 1.5f + sin(t * 3f) * r * 0.14f),
                strokeWidth = diameter * 0.022f,
                cap = StrokeCap.Round,
            )
        }
        // The three eyes.
        for ((dx, dy) in listOf(-0.28f to -0.16f, 0.02f to -0.30f, 0.24f to -0.10f)) {
            drawCircle(Island.CoconutDark, radius = r * 0.11f, center = Offset(r + dx * r, r + dy * r))
        }
        drawOval(
            color = Color.White.copy(alpha = 0.20f),
            topLeft = Offset(r * 0.35f, r * 0.30f),
            size = Size(r * 0.55f, r * 0.40f),
        )
    }
}

/** The beach she walks along: sand, a wet line, and a few tufts of grass. */
@Composable
fun Beach(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth()) {
        val top = size.height * 0.42f
        val sand = Path().apply {
            moveTo(0f, size.height)
            lineTo(0f, top + size.height * 0.14f)
            cubicTo(
                size.width * 0.32f, top - size.height * 0.10f,
                size.width * 0.68f, top + size.height * 0.22f,
                size.width, top - size.height * 0.02f,
            )
            lineTo(size.width, size.height)
            close()
        }
        drawPath(sand, Island.SandDark.copy(alpha = 0.42f))
        drawPath(
            sand,
            Island.Sea.copy(alpha = 0.30f),
            style = Stroke(width = size.height * 0.05f),
        )
        for (fraction in listOf(0.12f, 0.26f, 0.48f, 0.64f, 0.81f, 0.93f)) {
            val x = size.width * fraction
            for (blade in -1..1) {
                drawLine(
                    color = Island.PalmLeaf.copy(alpha = 0.40f),
                    start = Offset(x, size.height * 0.95f),
                    end = Offset(x + blade * size.height * 0.10f, size.height * 0.55f),
                    strokeWidth = size.height * 0.045f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

/**
 * Պույ-պույ chasing the coconut along the beach — the moment the tale starts.
 *
 * The coconut rolls ahead of her and she never quite catches it. Both turn around together at
 * each end, so she is always running forwards.
 */
@Composable
fun CoconutChase(height: Dp, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "chase")
    val progress by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "chase",
    )
    val bounce by transition.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(520, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bounce",
    )

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(height)
    ) {
        // Triangle wave: out to the right, then back to the left.
        val sweep = 1f - abs(progress * 2f - 1f)
        val facingLeft = progress > 0.5f
        val travel = maxWidth - height * 1.9f
        val coconutSize = height * 0.42f

        Box(
            Modifier
                .align(Alignment.BottomStart)
                .graphicsLayer {
                    translationX = travel.toPx() * sweep
                    scaleX = if (facingLeft) -1f else 1f
                }
        ) {
            PouyPouy(state = HelperState.Walking, size = height)
        }

        // The coconut stays a nose ahead, rolling as it goes.
        Canvas(
            Modifier
                .align(Alignment.BottomStart)
                .height(coconutSize)
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = travel.toPx() * sweep +
                        (if (facingLeft) -height.toPx() * 0.72f else height.toPx() * 0.86f)
                    translationY = -bounce * coconutSize.toPx() * 0.22f
                    rotationZ = (if (facingLeft) -1f else 1f) * sweep * 900f
                    transformOrigin = TransformOrigin(
                        pivotFractionX = coconutSize.toPx() / size.width / 2f,
                        pivotFractionY = 0.5f,
                    )
                }
        ) {
            drawCoconut(coconutSize.toPx())
        }
    }
}
