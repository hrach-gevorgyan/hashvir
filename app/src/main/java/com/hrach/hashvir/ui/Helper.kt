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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hrach.hashvir.theme.Mouse
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * Պույ-պույ Ճստունի.
 *
 * Built from real mouse parts — a pointed muzzle, whiskers, blushed cheeks, hands and feet,
 * a long curled tail — so she reads as a character rather than a stack of circles. Every part
 * is a separate layer, which is what lets her tilt, squash, point and shake independently.
 *
 * She is never still: breathing, blinking, and a tail that keeps swaying whatever else is
 * happening. She stays soft dove grey and never out-saturates the fruit.
 */
enum class HelperState {
    /** Between things: breathing, blinking, tail swaying, small head bob. */
    Idle,

    /** Intro: big wave, wide smile, ears up. */
    Waving,

    /** A question is on screen: head tilted, paw to chin, eyes up, ears forward. */
    Thinking,

    /** Right answer: squash-and-stretch hops, eyes squeezed shut, both arms up. */
    Happy,

    /** Wrong answer: slow head shake, frown, ears drooping. Never harsh. */
    Sad,

    /** Pointing at what she should look at, and looking there herself. */
    Suggesting,

    /** Strolling along the bottom of the screen, legs swinging. */
    Walking,
}

@Composable
fun PouyPouy(
    state: HelperState,
    size: Dp,
    modifier: Modifier = Modifier,
    speaking: Boolean = false,
) {
    val hop = remember { Animatable(0f) }
    val squash = remember { Animatable(0f) }
    val earLift = remember { Animatable(0f) }
    val lean = remember { Animatable(0f) }
    val arm = remember { Animatable(0f) }
    val leftArm = remember { Animatable(0f) }
    val shake = remember { Animatable(0f) }
    val smile = remember { Animatable(0.62f) }
    val lookX = remember { Animatable(0f) }
    val lookY = remember { Animatable(0f) }
    val eyesShut = remember { Animatable(0f) }

    val ambient = rememberInfiniteTransition(label = "ambient")
    val breath by ambient.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "breath",
    )
    val blink by ambient.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(3800, easing = LinearEasing), RepeatMode.Restart),
        label = "blink",
    )
    val tail by ambient.animateFloat(
        -1f, 1f,
        infiniteRepeatable(tween(1900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "tail",
    )
    val step by ambient.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(620, easing = LinearEasing), RepeatMode.Restart),
        label = "step",
    )
    // Roughly syllable rate: fast enough to read as speech, slow enough not to flutter.
    val talk by ambient.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(260, easing = LinearEasing), RepeatMode.Restart),
        label = "talk",
    )

    LaunchedEffect(state) {
        when (state) {
            HelperState.Idle -> {
                eyesShut.animateTo(0f, tween(150))
                earLift.animateTo(0f, tween(250))
                lean.animateTo(0f, tween(250))
                arm.animateTo(0f, tween(250))
                leftArm.animateTo(0f, tween(250))
                smile.animateTo(0.62f, tween(250))
                lookX.animateTo(0f, tween(400))
                lookY.animateTo(0f, tween(400))
                shake.snapTo(0f)
                hop.animateTo(0f, tween(200))
                squash.animateTo(0f, tween(200))
            }

            HelperState.Waving -> {
                eyesShut.animateTo(0f, tween(150))
                lean.animateTo(0.25f, tween(250))
                earLift.animateTo(0.6f, tween(250))
                smile.animateTo(1f, tween(250))
                arm.animateTo(
                    1f,
                    infiniteRepeatable(tween(380, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                )
            }

            HelperState.Thinking -> {
                eyesShut.animateTo(0f, tween(150))
                smile.animateTo(0.45f, tween(250))
                arm.animateTo(0.35f, tween(300, easing = FastOutSlowInEasing))
                earLift.animateTo(-0.35f, tween(300, easing = FastOutSlowInEasing))
                lean.animateTo(0.6f, tween(300, easing = FastOutSlowInEasing))
                // Eyes drift up and away, the way anyone looks when working something out.
                lookY.animateTo(-0.7f, tween(600, easing = FastOutSlowInEasing))
                lookX.animateTo(
                    0.5f,
                    infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                )
            }

            HelperState.Happy -> {
                lean.animateTo(0f, tween(120))
                smile.animateTo(1f, tween(150))
                earLift.animateTo(1f, spring(stiffness = Spring.StiffnessLow))
                // Both paws in the air when she is pleased.
                leftArm.animateTo(1f, tween(150))
                arm.animateTo(1f, tween(150))
                eyesShut.animateTo(1f, tween(150))
                lookY.animateTo(0f, tween(150))
                // Squash before each hop and stretch at the top: the whole read of "jumping".
                squash.animateTo(
                    0f,
                    keyframes {
                        durationMillis = 1000
                        0f at 0
                        0.35f at 90
                        -0.30f at 200
                        0f at 320
                        0.28f at 380
                        -0.22f at 470
                        0f at 580
                        0.18f at 640
                        -0.14f at 710
                        0f at 820
                    },
                )
            }

            HelperState.Sad -> {
                leftArm.animateTo(0f, tween(150))
                arm.animateTo(0f, tween(150))
                earLift.animateTo(-1f, tween(300))
                lean.animateTo(0f, tween(150))
                smile.animateTo(-0.55f, tween(250))
                eyesShut.animateTo(0.45f, tween(250))
                lookY.animateTo(0.6f, tween(300))
                // Slow, sympathetic head shake — "not that one" — never a buzz.
                shake.animateTo(
                    0f,
                    keyframes {
                        durationMillis = 900
                        0f at 0
                        -1f at 150
                        1f at 380
                        -0.8f at 600
                        0f at 900
                    },
                )
            }

            HelperState.Walking -> {
                leftArm.animateTo(0f, tween(200))
                eyesShut.animateTo(0f, tween(150))
                smile.animateTo(0.7f, tween(250))
                earLift.animateTo(0.25f, tween(250))
                lean.animateTo(0f, tween(250))
                arm.animateTo(0f, tween(250))
                lookY.animateTo(0f, tween(250))
            }

            HelperState.Suggesting -> {
                eyesShut.animateTo(0f, tween(150))
                smile.animateTo(0.6f, tween(250))
                earLift.animateTo(0.5f, tween(250))
                lean.animateTo(0.5f, tween(250))
                lookX.animateTo(0.8f, tween(400))
                arm.animateTo(
                    1f,
                    infiniteRepeatable(tween(640, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                )
            }
        }
    }

    // Every so often she gestures with one paw or the other, so a long stretch of listening
    // never looks like a frozen picture.
    LaunchedEffect(state, speaking) {
        if (state != HelperState.Idle && !speaking) return@LaunchedEffect
        var useLeft = false
        while (true) {
            delay(1800L + (0..2200).random())
            val hand = if (useLeft) leftArm else arm
            useLeft = !useLeft
            hand.animateTo(0.85f, tween(260, easing = FastOutSlowInEasing))
            hand.animateTo(0.45f, tween(200))
            hand.animateTo(0.85f, tween(200))
            hand.animateTo(0f, tween(320, easing = FastOutSlowInEasing))
        }
    }

    // Hops ride on the squash curve, so the feet leave the ground exactly when she stretches.
    LaunchedEffect(state) {
        if (state == HelperState.Happy) {
            hop.animateTo(
                0f,
                keyframes {
                    durationMillis = 1000
                    0f at 0
                    0f at 90
                    -1f at 200
                    0f at 320
                    0f at 380
                    -0.7f at 470
                    0f at 580
                    0f at 640
                    -0.4f at 710
                    0f at 820
                },
            )
        } else {
            hop.animateTo(0f, tween(200))
        }
    }

    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        val breathe = 1f + 0.020f * sin(breath * 2f * Math.PI.toFloat())
        val autoBlink = blink > 0.965f
        val lidClose = maxOf(eyesShut.value, if (autoBlink) 1f else 0f)
        // While she is talking the jaw moves and the whole head nods very slightly.
        val mouthOpen = if (speaking) (0.35f + 0.65f * abs(sin(talk * Math.PI.toFloat()))) else 0f
        val walking = state == HelperState.Walking
        val walkPhase = if (walking) step else 0f
        // Walking bounces twice per stride, once for each foot.
        val bob = when {
            walking -> -kotlin.math.abs(sin(step * 2f * Math.PI.toFloat())) * s * 0.030f
            state == HelperState.Idle -> sin(breath * 2f * Math.PI.toFloat()) * s * 0.006f
            else -> 0f
        }

        translate(0f, hop.value * s * 0.14f + bob) {
            // Squash horizontally and stretch vertically about the feet.
            scale(
                scaleX = 1f + squash.value * 0.16f,
                scaleY = 1f - squash.value * 0.16f,
                pivot = Offset(this.size.width / 2f, this.size.height * 0.97f),
            ) {
                rotate(
                    degrees = 6f * lean.value + 10f * shake.value,
                    pivot = Offset(this.size.width / 2f, this.size.height),
                ) {
                    drawMouse(
                        breathe = breathe,
                        earLift = earLift.value,
                        arm = arm.value,
                        lidClose = lidClose,
                        smile = smile.value,
                        lookX = lookX.value,
                        lookY = lookY.value,
                        tailSway = tail,
                        walkPhase = walkPhase,
                        mouthOpen = mouthOpen,
                        leftArm = leftArm.value,
                        // Talking lifts the corners of her mouth further: she sounds pleased,
                        // so she should look it.
                        extraSmile = if (speaking) 0.25f else 0f,
                    )
                }
            }
        }
    }
}

private val Fur = Mouse.Body
private val FurDark = Color(0xFFA49DB5)
private val Blush = Color(0x66F2A0AC)

private fun DrawScope.drawMouse(
    breathe: Float,
    earLift: Float,
    arm: Float,
    lidClose: Float,
    smile: Float,
    lookX: Float,
    lookY: Float,
    tailSway: Float,
    walkPhase: Float,
    mouthOpen: Float,
    leftArm: Float,
    extraSmile: Float,
) {
    val s = size.minDimension
    val cx = size.width / 2f
    val ground = s * 0.95f

    // Tail: long, curled, always drifting. Never below 2dp or it vanishes on pastel.
    val tailPath = Path().apply {
        moveTo(cx + s * 0.20f, ground - s * 0.08f)
        cubicTo(
            cx + s * 0.46f, ground - s * 0.02f + tailSway * s * 0.03f,
            cx + s * 0.50f, ground - s * 0.34f - tailSway * s * 0.04f,
            cx + s * 0.30f, ground - s * 0.34f + tailSway * s * 0.02f,
        )
    }
    drawPath(tailPath, FurDark, style = Stroke(width = maxOf(2.dp.toPx(), s * 0.032f), cap = StrokeCap.Round))

    // Feet, so she is standing on something rather than floating. While walking they swing
    // in opposite phase and lift off the ground in turn.
    for ((index, side) in listOf(-1f, 1f).withIndex()) {
        val phase = walkPhase * 2f * Math.PI.toFloat() + index * Math.PI.toFloat()
        val swing = if (walkPhase == 0f) 0f else sin(phase) * s * 0.10f
        val lift = if (walkPhase == 0f) 0f else maxOf(0f, sin(phase)) * s * 0.045f
        drawOval(
            color = FurDark,
            topLeft = Offset(cx + side * s * 0.13f - s * 0.075f + swing, ground - s * 0.065f - lift),
            size = Size(s * 0.15f, s * 0.085f),
        )
    }

    // Ears: the character. Big, and they lift, droop and spread with mood.
    val earRadius = s * 0.185f
    val earY = s * 0.27f - s * 0.055f * earLift
    val earSpread = s * 0.215f + s * 0.025f * earLift
    for (side in listOf(-1f, 1f)) {
        val ex = cx + side * earSpread
        drawCircle(FurDark, radius = earRadius * 1.03f, center = Offset(ex, earY + earRadius * 0.05f))
        drawCircle(Fur, radius = earRadius, center = Offset(ex, earY))
        drawCircle(Mouse.Ear, radius = earRadius * 0.62f, center = Offset(ex, earY + earRadius * 0.04f))
        drawCircle(
            Color(0x33FFFFFF),
            radius = earRadius * 0.36f,
            center = Offset(ex - side * earRadius * 0.14f, earY - earRadius * 0.16f),
        )
    }

    // Body: a narrow pear. She is a small thin mouse, not a ball.
    val bodyW = s * 0.44f * breathe
    val bodyH = s * 0.60f * breathe
    drawOval(
        color = Fur,
        topLeft = Offset(cx - bodyW / 2f, ground - bodyH),
        size = Size(bodyW, bodyH),
    )
    // Lighter belly, and a soft shadow where she meets the ground.
    drawOval(
        color = Color(0x4DFFFFFF),
        topLeft = Offset(cx - bodyW * 0.32f, ground - bodyH * 0.70f),
        size = Size(bodyW * 0.64f, bodyH * 0.64f),
    )
    drawOval(
        color = Color(0x1A3A4454),
        topLeft = Offset(cx - bodyW * 0.62f, ground - s * 0.035f),
        size = Size(bodyW * 1.24f, s * 0.055f),
    )

    val headR = s * 0.225f * breathe
    val headY = s * 0.44f
    drawCircle(Fur, radius = headR, center = Offset(cx, headY))

    // Muzzle: the piece that makes her a mouse and not a bear.
    val muzzleY = headY + headR * 0.42f
    drawOval(
        color = Color(0x55FFFFFF),
        topLeft = Offset(cx - headR * 0.52f, muzzleY - headR * 0.28f),
        size = Size(headR * 1.04f, headR * 0.62f),
    )

    // Cheeks.
    for (side in listOf(-1f, 1f)) {
        drawCircle(Blush, radius = headR * 0.20f, center = Offset(cx + side * headR * 0.62f, muzzleY - headR * 0.05f))
    }

    // Whiskers: three a side, thin and swept back.
    val whiskerX = cx
    for (side in listOf(-1f, 1f)) {
        for ((index, tilt) in listOf(-0.18f, 0f, 0.18f).withIndex()) {
            val startX = whiskerX + side * headR * 0.34f
            val startY = muzzleY + headR * 0.02f + index * headR * 0.10f - headR * 0.10f
            drawLine(
                color = Mouse.Detail.copy(alpha = 0.55f),
                start = Offset(startX, startY),
                end = Offset(startX + side * headR * 0.85f, startY + tilt * headR * 1.1f),
                strokeWidth = maxOf(1.2f.dp.toPx(), s * 0.008f),
                cap = StrokeCap.Round,
            )
        }
    }

    // Brows: gentle arcs, always curving upward. Straight brows angled in toward the nose
    // are the universal angry face, which is exactly what she must never look like.
    for (side in listOf(-1f, 1f)) {
        val bx = cx + side * headR * 0.40f
        // Worried when sad: both brows lift at the inner end. Never lowered.
        val by = headY - headR * 0.54f - (if (smile < 0f) headR * 0.04f else 0f)
        val brow = Path().apply {
            moveTo(bx - headR * 0.15f, by + headR * 0.05f)
            quadraticBezierTo(bx, by - headR * 0.07f, bx + headR * 0.15f, by + headR * 0.05f)
        }
        drawPath(
            brow,
            Mouse.Detail.copy(alpha = 0.32f),
            style = Stroke(width = s * 0.012f, cap = StrokeCap.Round),
        )
    }

    // Nose.
    drawCircle(Color(0xFFE58A9A), radius = headR * 0.13f, center = Offset(cx, muzzleY - headR * 0.06f))
    drawCircle(Color(0x55FFFFFF), radius = headR * 0.045f, center = Offset(cx - headR * 0.04f, muzzleY - headR * 0.10f))

    // Mouth: a curve from wide grin to small frown, or an open jaw while she is speaking.
    val mouthY = muzzleY + headR * 0.20f
    val mouthW = headR * 0.30f
    if (mouthOpen > 0.01f) {
        val open = headR * 0.22f * mouthOpen
        val jaw = Path().apply {
            moveTo(cx - mouthW, mouthY)
            quadraticBezierTo(cx, mouthY + open * 1.7f, cx + mouthW, mouthY)
            quadraticBezierTo(cx, mouthY - open * 0.28f, cx - mouthW, mouthY)
            close()
        }
        drawPath(jaw, Color(0xFF7A5560))
        // Tongue, so the open mouth does not read as a hole.
        val tongue = Path().apply {
            moveTo(cx - mouthW * 0.52f, mouthY + open * 0.55f)
            quadraticBezierTo(cx, mouthY + open * 1.55f, cx + mouthW * 0.52f, mouthY + open * 0.55f)
            quadraticBezierTo(cx, mouthY + open * 0.30f, cx - mouthW * 0.52f, mouthY + open * 0.55f)
            close()
        }
        drawPath(tongue, Color(0xFFE07A8C))
    } else {
        val mouth = Path().apply {
            moveTo(cx - mouthW, mouthY)
            quadraticBezierTo(
                cx,
                mouthY + (smile + extraSmile).coerceAtMost(1.2f) * headR * 0.30f,
                cx + mouthW,
                mouthY,
            )
        }
        drawPath(mouth, Mouse.Detail, style = Stroke(width = s * 0.016f, cap = StrokeCap.Round))
    }

    // Eyes: big, with pupils that actually look somewhere, and lids that close.
    val eyeR = headR * 0.24f
    val eyeY = headY - headR * 0.16f
    for (side in listOf(-1f, 1f)) {
        val ex = cx + side * headR * 0.40f
        if (lidClose > 0.92f) {
            // Squeezed shut: a happy arc, not a flat line.
            val arc = Path().apply {
                moveTo(ex - eyeR, eyeY + eyeR * 0.25f)
                quadraticBezierTo(ex, eyeY - eyeR * 0.65f, ex + eyeR, eyeY + eyeR * 0.25f)
            }
            drawPath(arc, Mouse.Detail, style = Stroke(width = s * 0.018f, cap = StrokeCap.Round))
        } else {
            drawCircle(Color.White, radius = eyeR, center = Offset(ex, eyeY))
            drawCircle(Mouse.Detail, radius = eyeR, center = Offset(ex, eyeY), style = Stroke(width = s * 0.008f))
            val pupil = Offset(ex + lookX * eyeR * 0.38f, eyeY + lookY * eyeR * 0.38f)
            drawCircle(Mouse.Detail, radius = eyeR * 0.56f, center = pupil)
            drawCircle(Color.White, radius = eyeR * 0.20f, center = Offset(pupil.x + eyeR * 0.20f, pupil.y - eyeR * 0.22f))
            if (lidClose > 0.01f) {
                // Half-lidded: a sad, heavy eye.
                drawOval(
                    color = Fur,
                    topLeft = Offset(ex - eyeR * 1.05f, eyeY - eyeR * 1.1f),
                    size = Size(eyeR * 2.1f, eyeR * 1.15f * lidClose),
                )
            }
        }
    }

    // Arms last, so the wave is never buried behind the head.
    val shoulderY = ground - bodyH * 0.62f
    if (leftArm > 0.01f) {
        val shoulder = Offset(cx - bodyW * 0.40f, shoulderY)
        val reach = s * 0.28f
        val angle = (180f + 30f + 55f * leftArm) * (Math.PI / 180f).toFloat()
        val paw = Offset(shoulder.x + reach * cos(angle), shoulder.y + reach * sin(angle))
        drawLine(Fur, shoulder, paw, strokeWidth = s * 0.078f, cap = StrokeCap.Round)
        drawCircle(Fur, radius = s * 0.055f, center = paw)
        drawCircle(Blush, radius = s * 0.028f, center = paw)
    } else {
        drawLine(
            Fur,
            Offset(cx - bodyW * 0.40f, shoulderY),
            Offset(cx - bodyW * 0.52f, shoulderY + s * 0.10f),
            strokeWidth = s * 0.075f,
            cap = StrokeCap.Round,
        )
    }
    if (arm > 0.01f) {
        val shoulder = Offset(cx + bodyW * 0.40f, shoulderY)
        val reach = s * 0.28f
        val angle = (-30f - 60f * arm) * (Math.PI / 180f).toFloat()
        val paw = Offset(shoulder.x + reach * cos(angle), shoulder.y + reach * sin(angle))
        drawLine(Fur, shoulder, paw, strokeWidth = s * 0.078f, cap = StrokeCap.Round)
        drawCircle(Fur, radius = s * 0.055f, center = paw)
        drawCircle(Blush, radius = s * 0.028f, center = paw)
    } else {
        drawLine(
            Fur,
            Offset(cx + bodyW * 0.40f, shoulderY),
            Offset(cx + bodyW * 0.52f, shoulderY + s * 0.10f),
            strokeWidth = s * 0.075f,
            cap = StrokeCap.Round,
        )
    }
}
