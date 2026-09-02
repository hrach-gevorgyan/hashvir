package com.hrach.hashvir.ui

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hrach.hashvir.BuildConfig
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.game.Layout
import com.hrach.hashvir.game.RecognitionRound
import com.hrach.hashvir.theme.BackgroundTint
import com.hrach.hashvir.theme.Feedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

private const val CARD_GAP_DP = 10
private const val SHAKE_DP = 8
private const val SHAKE_MS = 200
private const val PRAISE_TO_NEXT_MS = 1500L

/**
 * «Ո՞րն է X-ը» — Պույ-պույ asks, three cards answer.
 *
 * A wrong tap is not a failure: the card wobbles amber, she hears a soft "hmm", and the cards
 * stay exactly where they are so she can try again. Nothing is taken away and nothing is scored.
 */
@Composable
fun RecognitionScreen(
    round: RecognitionRound,
    sounds: SoundBank,
    onRoundFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var solvedAt by remember(round) { mutableStateOf<Offset?>(null) }

    LaunchedEffect(round) {
        sounds.play("ask_${round.answer}")
    }

    LaunchedEffect(solvedAt) {
        if (solvedAt == null) return@LaunchedEffect
        sounds.play("praise_${Random.nextInt(1, 5)}")
        delay(PRAISE_TO_NEXT_MS)
        onRoundFinished()
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(round.background.color)
    ) {
        val density = LocalDensity.current
        val screenHeight = maxHeight
        val gap = CARD_GAP_DP.dp
        val cardWidth = (maxWidth - gap * 4) / 3
        // The wobble is specified in dp, so it has to be converted, not hardcoded in pixels.
        val shakePx = with(density) { SHAKE_DP.dp.toPx() }
        val cardHeight = maxOf(cardWidth * 1.3f, Layout.Floor)

        if (BuildConfig.DEBUG && cardWidth < Layout.Floor) {
            Log.w(
                "Layout",
                "recognition card ${cardWidth.value.toInt()}dp wide is below the " +
                    "${Layout.Floor.value.toInt()}dp floor in ${maxWidth.value.toInt()}dp",
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = gap),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            round.choices.forEachIndexed { index, choice ->
                val centre = with(density) {
                    Offset(
                        (gap + (cardWidth + gap) * index + cardWidth / 2f).toPx(),
                        (screenHeight / 2f).toPx(),
                    )
                }
                ChoiceCard(
                    choice = choice,
                    correct = choice == round.answer,
                    solved = solvedAt != null,
                    sounds = sounds,
                    glyphHeight = cardHeight * 0.45f,
                    shakeDistance = shakePx,
                    onSolved = { solvedAt = centre },
                    modifier = Modifier
                        .width(cardWidth)
                        .height(cardHeight),
                )
            }
        }

        solvedAt?.let { Confetti(origin = it) }

        PouyPouy(
            state = if (solvedAt == null) HelperState.Asking else HelperState.Happy,
            size = maxHeight * 0.15f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(maxWidth * 0.04f),
        )
    }
}

@Composable
private fun ChoiceCard(
    choice: Int,
    correct: Boolean,
    solved: Boolean,
    sounds: SoundBank,
    glyphHeight: Dp,
    shakeDistance: Float,
    onSolved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shake = remember { Animatable(0f) }
    val wrongTint = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier
            .offset { IntOffset(shake.value.roundToInt(), 0) }
            .clip(RoundedCornerShape(20.dp))
            .background(BackgroundTint.Paper.color)
            .background(Feedback.Neutral.copy(alpha = 0.28f * wrongTint.value))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !solved,
            ) {
                if (correct) {
                    onSolved()
                } else {
                    sounds.play("oops_${Random.nextInt(1, 3)}")
                    scope.launch { wobble(shake, wrongTint, shakeDistance) }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        NumberGlyph(count = choice, glyphHeight = glyphHeight)
    }
}

/** Three cycles of +-8dp inside 200ms, then still. Amber, never red. */
private suspend fun wobble(
    shake: Animatable<Float, *>,
    tint: Animatable<Float, *>,
    distance: Float,
) {
    tint.snapTo(1f)
    shake.animateTo(
        0f,
        keyframes {
            durationMillis = SHAKE_MS
            0f at 0
            -distance at 33
            distance at 67
            -distance at 100
            distance at 133
            -distance at 167
            0f at SHAKE_MS
        },
    )
    tint.animateTo(0f, tween(120))
}
