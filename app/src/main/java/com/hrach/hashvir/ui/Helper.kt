package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hrach.hashvir.theme.Mouse
import kotlin.math.cos
import kotlin.math.sin

/**
 * Պույ-պույ Ճստունի, the face the voice comes from.
 *
 * She is alive the whole time: a slow breath, an occasional blink, and a distinct reaction to
 * everything the child does. Body is one shape; ears, arm and tail are separate layers so they
 * move independently.
 *
 * She stays soft dove grey and is never more saturated than the fruit.
 */
enum class HelperState {
    /** Between things: breathing and blinking, nothing more. */
    Idle,

    /** Intro: one arm up, waving. */
    Waving,

    /** A question is on screen: ears forward, leaning in. */
    Thinking,

    /** Right answer: three hops, ears flying, arm up. */
    Happy,

    /** Wrong answer: a slow head shake and drooping ears. Never harsh. */
    Sad,

    /** Pointing at what she should look at. */
    Suggesting,
}

@Composable
fun PouyPouy(state: HelperState, size: Dp, modifier: Modifier = Modifier) {
    val hop = remember { Animatable(0f) }
    val earLift = remember { Animatable(0f) }
    val lean = remember { Animatable(0f) }
    val arm = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }

    // Always breathing, always blinking, so she never looks like a frozen picture.
    val ambient = rememberInfiniteTransition(label = "ambient")
    val breath by ambient.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "breath",
    )
    val blink by ambient.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "blink",
    )

    LaunchedEffect(state) {
        when (state) {
            HelperState.Idle -> {
                earLift.animateTo(0f, tween(200))
                lean.animateTo(0f, tween(200))
                arm.animateTo(0f, tween(200))
                shake.snapTo(0f)
                hop.animateTo(0f, tween(200))
            }

            HelperState.Waving -> {
                lean.animateTo(0f, tween(150))
                earLift.animateTo(0.5f, tween(250))
                arm.animateTo(
                    1f,
                    infiniteRepeatable(tween(420, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                )
            }

            HelperState.Thinking -> {
                arm.animateTo(0.45f, tween(300, easing = FastOutSlowInEasing))
                earLift.animateTo(-0.8f, tween(300, easing = FastOutSlowInEasing))
                lean.animateTo(1f, tween(300, easing = FastOutSlowInEasing))
            }

            HelperState.Happy -> {
                lean.animateTo(0f, tween(120))
                arm.animateTo(0.8f, tween(150))
                earLift.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
                // Three hops, each smaller than the last.
                hop.animateTo(
                    0f,
                    keyframes {
                        durationMillis = 900
                        0f at 0
                        -1f at 150
                        0f at 300
                        -0.7f at 450
                        0f at 580
                        -0.4f at 700
                        0f at 800
                    },
                )
                arm.animateTo(0f, tween(200))
            }

            HelperState.Sad -> {
                arm.animateTo(0f, tween(150))
                earLift.animateTo(-1f, tween(250))
                lean.animateTo(0f, tween(150))
                // A slow head shake, not a buzz.
                shake.animateTo(
                    0f,
                    keyframes {
                        durationMillis = 700
                        0f at 0
                        -1f at 120
                        1f at 290
                        -1f at 460
                        0f at 700
                    },
                )
            }

            HelperState.Suggesting -> {
                earLift.animateTo(0.6f, tween(250))
                lean.animateTo(0.5f, tween(250))
                arm.animateTo(
                    1f,
                    infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                )
            }
        }
    }

    Canvas(modifier.size(size)) {
        val breathScale = 1f + 0.022f * sin(breath * 2f * Math.PI.toFloat())
        val eyesShut = blink > 0.96f
        val hopPx = hop.value * this.size.minDimension * 0.13f

        translate(0f, hopPx) {
            rotate(
                degrees = 7f * lean.value + 9f * shake.value,
                pivot = Offset(this.size.width / 2f, this.size.height),
            ) {
                drawMouse(breathScale, earLift.value, arm.value, eyesShut)
            }
        }
    }
}

private fun DrawScope.drawMouse(breath: Float, earLift: Float, arm: Float, eyesShut: Boolean) {
    val s = size.minDimension
    val cx = size.width / 2f

    // The ears are the character: a mouse silhouette without big ears reads as a blob.
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
        cubicTo(cx + s * 0.48f, s * 0.92f, cx + s * 0.46f, s * 0.58f, cx + s * 0.30f, s * 0.62f)
    }
    drawPath(tail, Mouse.Body, style = Stroke(width = maxOf(2.dp.toPx(), s * 0.035f)))

    val bodyW = s * 0.62f * breath
    val bodyH = s * 0.60f * breath
    drawOval(
        color = Mouse.Body,
        topLeft = Offset(cx - bodyW / 2f, s * 0.94f - bodyH),
        size = Size(bodyW, bodyH),
    )

    val headR = s * 0.26f * breath
    val headY = s * 0.42f
    drawCircle(Mouse.Body, radius = headR, center = Offset(cx, headY))

    val eyeY = headY - headR * 0.10f
    for (side in listOf(-1f, 1f)) {
        val ex = cx + side * headR * 0.42f
        if (eyesShut) {
            drawLine(
                Mouse.Detail,
                Offset(ex - s * 0.035f, eyeY),
                Offset(ex + s * 0.035f, eyeY),
                strokeWidth = s * 0.022f,
                cap = StrokeCap.Round,
            )
        } else {
            drawCircle(Mouse.Detail, radius = s * 0.035f, center = Offset(ex, eyeY))
            drawCircle(
                Color.White,
                radius = s * 0.013f,
                center = Offset(ex + s * 0.012f, eyeY - s * 0.012f),
            )
        }
    }
    drawCircle(Mouse.Ear, radius = s * 0.045f, center = Offset(cx, headY + headR * 0.55f))

    // Arm last, so it is never buried behind the head, and swung clear of it.
    if (arm > 0.01f) {
        val shoulder = Offset(cx + s * 0.30f, s * 0.66f)
        val reach = s * 0.30f
        val angle = (-35f - 55f * arm) * (Math.PI / 180f).toFloat()
        val paw = Offset(shoulder.x + reach * cos(angle), shoulder.y + reach * sin(angle))
        drawLine(Mouse.Body, shoulder, paw, strokeWidth = s * 0.085f, cap = StrokeCap.Round)
        drawCircle(Mouse.Body, radius = s * 0.058f, center = paw)
        drawCircle(Mouse.Ear, radius = s * 0.030f, center = paw)
    }
}
