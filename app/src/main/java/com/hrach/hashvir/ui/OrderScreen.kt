package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.audio.rememberSpeaking
import com.hrach.hashvir.game.Layout
import com.hrach.hashvir.game.OrderRound
import com.hrach.hashvir.theme.Armenian
import com.hrach.hashvir.theme.Feedback
import com.hrach.hashvir.theme.Fruit
import com.hrach.hashvir.theme.NumberColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val SHAKE_DP = 8
private const val SHAKE_MS = 200
private const val AFTER_LAST_MS = 700L
private const val AFTER_PRAISE_MS = 1250L
private const val AFTER_CHIME_MS = 1100L

/** After this many wrong taps the next number starts pulsing, rather than leaving her stuck. */
private const val HINT_AFTER_WRONG = 1

/**
 * Հերթով — the numbers scattered, tapped in order.
 *
 * This is ordinality: that numbers come in a fixed sequence and each has a place in it, not
 * just a name. Cardinality answers "how many"; this answers "what comes next".
 *
 * A wrong tap costs nothing. The number wobbles amber, Պույ-պույ shakes his head, and after
 * the first mistake the number she is looking for begins to pulse, so she is never stuck.
 */
@Composable
fun OrderScreen(
    round: OrderRound,
    sounds: SoundBank,
    compact: Boolean,
    onRoundFinished: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var next by remember(round) { mutableIntStateOf(1) }
    var wrongTaps by remember(round) { mutableIntStateOf(0) }
    var lastWrongAt by remember(round) { mutableStateOf(0L) }
    val complete = next > round.count

    LaunchedEffect(complete) {
        if (!complete) return@LaunchedEffect
        delay(AFTER_LAST_MS)
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
        val playHeight = Layout.playHeight(maxHeight)
        val diameter = Layout.diameter(round.count, maxWidth, playHeight, compact)
        val shakePx = with(LocalDensity.current) { SHAKE_DP.dp.toPx() }

        Scenery(seed = round.count * 17, strength = 0.55f)

        for ((index, position) in round.positions.withIndex()) {
            val number = index + 1
            NumberBubble(
                number = number,
                diameter = diameter,
                done = number < next,
                // The one she is looking for, once she has already tried something else.
                hinted = number == next && wrongTaps >= HINT_AFTER_WRONG,
                shakeDistance = shakePx,
                wrongKey = lastWrongAt,
                onTap = {
                    when {
                        number < next -> Unit
                        number == next -> {
                            sounds.play("num_$number")
                            next += 1
                        }

                        else -> {
                            sounds.playOops()
                            wrongTaps += 1
                            lastWrongAt = System.nanoTime()
                        }
                    }
                },
                modifier = Modifier.offset(
                    x = maxWidth * position.x - diameter / 2,
                    y = playHeight * position.y - diameter / 2,
                ),
            )
        }

        if (complete) {
            Confetti(
                origin = with(LocalDensity.current) {
                    Offset((maxWidth / 2f).toPx(), (playHeight / 2f).toPx())
                }
            )
        }

        PouyPouy(
            state = when {
                complete -> HelperState.Happy
                wrongTaps > 0 && !complete -> HelperState.Suggesting
                else -> HelperState.Idle
            },
            speaking = rememberSpeaking(sounds),
            size = maxHeight * 0.15f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
        )

        BackButton(
            onBack = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
        )
    }
}

@Composable
private fun NumberBubble(
    number: Int,
    diameter: Dp,
    done: Boolean,
    hinted: Boolean,
    shakeDistance: Float,
    wrongKey: Long,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shake = remember { Animatable(0f) }
    val pop = remember { Animatable(1f) }
    val pulse = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    var tappedWrong by remember { mutableStateOf(false) }

    LaunchedEffect(done) {
        if (done) {
            pop.animateTo(1.2f, tween(120))
            pop.animateTo(0.85f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        } else {
            pop.snapTo(1f)
        }
    }

    // Only the bubble that was actually mistapped wobbles.
    LaunchedEffect(wrongKey) {
        if (!tappedWrong) return@LaunchedEffect
        tappedWrong = false
        shake.animateTo(
            0f,
            keyframes {
                durationMillis = SHAKE_MS
                0f at 0
                -shakeDistance at 33
                shakeDistance at 67
                -shakeDistance at 100
                shakeDistance at 133
                -shakeDistance at 167
                0f at SHAKE_MS
            },
        )
    }

    LaunchedEffect(hinted) {
        if (hinted) {
            while (true) {
                pulse.animateTo(1.12f, tween(520, easing = FastOutSlowInEasing))
                pulse.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
            }
        } else {
            pulse.snapTo(1f)
        }
    }

    Box(
        modifier
            .offset { IntOffset(shake.value.roundToInt(), 0) }
            .size(diameter)
            .scale(pop.value * pulse.value)
            .alpha(if (done) 0.45f else 1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !done,
            ) {
                tappedWrong = true
                onTap()
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val stroke = Fruit.OutlineWidthDp.dp.toPx()
            drawCircle(NumberColors[number], radius = radius)
            drawCircle(
                if (done) Feedback.Positive else Fruit.OutlineColor,
                radius = radius - stroke / 2f,
                style = Stroke(width = stroke),
            )
            if (done) {
                // A tick, so a finished number reads as done rather than merely faded.
                val r = radius
                drawLine(
                    Color.White,
                    Offset(r * 0.62f, r * 1.02f),
                    Offset(r * 0.90f, r * 1.34f),
                    strokeWidth = r * 0.16f,
                    cap = StrokeCap.Round,
                )
                drawLine(
                    Color.White,
                    Offset(r * 0.90f, r * 1.34f),
                    Offset(r * 1.42f, r * 0.68f),
                    strokeWidth = r * 0.16f,
                    cap = StrokeCap.Round,
                )
            }
        }
        if (!done) {
            Text(
                text = number.toString(),
                color = Color.White,
                fontFamily = Armenian,
                fontWeight = FontWeight.Black,
                fontSize = (diameter.value * 0.52f).sp,
                lineHeight = (diameter.value * 0.56f).sp,
                maxLines = 1,
            )
        }
    }
}
