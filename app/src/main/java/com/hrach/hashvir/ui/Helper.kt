package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hrach.hashvir.theme.Mouse

/**
 * Պույ-պույ. The face the voice comes from, not a companion — tapping her does nothing.
 *
 * Visible and animating only at a round boundary. While objects are tappable she is
 * [HelperState.Still]: shrunk into a corner and completely motionless, so she never competes
 * with the count.
 *
 * She is soft dove grey throughout and is never more saturated than the countable objects.
 */
enum class HelperState {
    /** Minimised in a corner during counting. No animation at all. */
    Still,

    /** Round completion: both ears perk, whole body bounces. */
    Happy,

    /** Start of a recognition round: ears rotate forward, slight lean in. */
    Asking,
}

@Composable
fun PouyPouy(state: HelperState, size: Dp, modifier: Modifier = Modifier) {
    val bounce = remember { Animatable(1f) }
    val earLift = remember { Animatable(0f) }
    val lean = remember { Animatable(0f) }

    LaunchedEffect(state) {
        when (state) {
            HelperState.Still -> {
                bounce.snapTo(1f)
                earLift.snapTo(0f)
                lean.snapTo(0f)
            }

            HelperState.Happy -> {
                lean.animateTo(0f, tween(200))
                earLift.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
                bounce.animateTo(
                    1f,
                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    initialVelocity = 6f,
                )
            }

            HelperState.Asking -> {
                earLift.animateTo(-0.8f, tween(300, easing = FastOutSlowInEasing))
                lean.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
            }
        }
    }

    Canvas(modifier.size(size)) {
        rotate(degrees = 7f * lean.value, pivot = Offset(this.size.width / 2f, this.size.height)) {
            drawMouse(bounce.value, earLift.value)
        }
    }
}

/**
 * Body is one static shape; ears and tail are separate layers and carry all the movement.
 * Drawn as paths rather than a res/drawable vector so those layers can animate independently.
 */
private fun DrawScope.drawMouse(bounce: Float, earLift: Float) {
    val s = size.minDimension
    val cx = size.width / 2f

    // The ears are the character — a mouse silhouette without big ears reads as a blob.
    val earRadius = s * 0.22f
    val earY = s * 0.26f - s * 0.06f * earLift
    val earSpread = s * 0.26f + s * 0.02f * earLift

    for (side in listOf(-1f, 1f)) {
        val ex = cx + side * earSpread
        drawCircle(Mouse.Body, radius = earRadius, center = Offset(ex, earY))
        drawCircle(Mouse.Ear, radius = earRadius * 0.58f, center = Offset(ex, earY))
    }

    // Tail: thin and curled, never below 2dp or it disappears on a pastel ground.
    val tail = Path().apply {
        moveTo(cx + s * 0.24f, s * 0.86f)
        cubicTo(
            cx + s * 0.48f, s * 0.92f,
            cx + s * 0.46f, s * 0.58f,
            cx + s * 0.30f, s * 0.62f,
        )
    }
    drawPath(tail, Mouse.Body, style = Stroke(width = maxOf(2.dp.toPx(), s * 0.035f)))

    // Body: one static sitting shape, scaled by the bounce.
    val bodyW = s * 0.62f * bounce
    val bodyH = s * 0.60f * bounce
    drawOval(
        color = Mouse.Body,
        topLeft = Offset(cx - bodyW / 2f, s * 0.94f - bodyH),
        size = Size(bodyW, bodyH),
    )

    val headR = s * 0.26f * bounce
    val headY = s * 0.42f
    drawCircle(Mouse.Body, radius = headR, center = Offset(cx, headY))

    val eyeY = headY - headR * 0.10f
    for (side in listOf(-1f, 1f)) {
        drawCircle(
            Mouse.Detail,
            radius = s * 0.035f,
            center = Offset(cx + side * headR * 0.42f, eyeY),
        )
    }
    drawCircle(Mouse.Ear, radius = s * 0.045f, center = Offset(cx, headY + headR * 0.55f))
}
