package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.audio.rememberSpeaking
import com.hrach.hashvir.game.RecognitionRound
import com.hrach.hashvir.game.numberWord
import com.hrach.hashvir.theme.Armenian
import com.hrach.hashvir.theme.BackgroundTint
import com.hrach.hashvir.theme.Feedback
import com.hrach.hashvir.theme.Ink
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val CARD_GAP_DP = 12
private const val SHAKE_DP = 8
private const val SHAKE_MS = 200
private const val PRAISE_TO_NEXT_MS = 1600L

/**
 * Գուշակել — «Ո՞րն է X-ը». Four cards in a two-by-two grid, one of them right.
 *
 * A wrong tap is not a failure: the card wobbles amber, Պույ-պույ shakes his head, and every
 * card stays exactly where it is so she can try again. Nothing is scored, nothing disappears.
 */
@Composable
fun RecognitionScreen(
    round: RecognitionRound,
    sounds: SoundBank,
    onRoundFinished: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var solvedAt by remember(round) { mutableStateOf<Offset?>(null) }
    var wrongAt by remember(round) { mutableStateOf(0) }
    var ruledOut by remember(round) { mutableStateOf(emptySet<Int>()) }

    LaunchedEffect(round) {
        delay(200)
        sounds.play("ask_${round.answer}")
    }

    LaunchedEffect(solvedAt) {
        if (solvedAt == null) return@LaunchedEffect
        sounds.playPraise()
        delay(PRAISE_TO_NEXT_MS)
        onRoundFinished()
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(round.background.color)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val density = LocalDensity.current
        val screenHeight = maxHeight
        val gap = CARD_GAP_DP.dp
        val cardWidth = (maxWidth - gap * 3) / 2
        val cardHeight = minOf(cardWidth, (screenHeight * 0.60f - gap) / 2)
        val shakePx = with(density) { SHAKE_DP.dp.toPx() }

        Scenery(seed = round.answer)

        Column(
            Modifier
                .align(Alignment.Center)
                .padding(horizontal = gap, vertical = gap)
                .padding(bottom = screenHeight * 0.08f),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            for (row in 0 until 2) {
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    for (column in 0 until 2) {
                        val index = row * 2 + column
                        val choice = round.choices[index]
                        val centre = with(density) {
                            Offset(
                                (gap + (cardWidth + gap) * column + cardWidth / 2f).toPx(),
                                (screenHeight / 2f - cardHeight / 2f - gap / 2f +
                                    (cardHeight + gap) * row).toPx(),
                            )
                        }
                        ChoiceCard(
                            choice = choice,
                            correct = choice == round.answer,
                            solved = solvedAt != null,
                            glyphHeight = cardHeight * 0.52f,
                            shakeDistance = shakePx,
                            onCorrect = { solvedAt = centre },
                            ruledOut = choice in ruledOut,
                            onWrong = {
                                sounds.playOops()
                                ruledOut = ruledOut + choice
                                wrongAt += 1
                            },
                            modifier = Modifier
                                .width(cardWidth)
                                .height(cardHeight),
                        )
                    }
                }
            }
        }

        solvedAt?.let { Confetti(origin = it) }

        PouyPouy(
            state = when {
                solvedAt != null -> HelperState.Happy
                wrongAt > 0 -> HelperState.Sad
                else -> HelperState.Thinking
            },
            size = maxHeight * 0.19f,
            speaking = rememberSpeaking(sounds),
            modifier = Modifier
                .align(Alignment.BottomEnd)
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
private fun ChoiceCard(
    choice: Int,
    correct: Boolean,
    solved: Boolean,
    ruledOut: Boolean,
    glyphHeight: Dp,
    shakeDistance: Float,
    onCorrect: () -> Unit,
    onWrong: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shake = remember { Animatable(0f) }
    val wrongTint = remember { Animatable(0f) }
    val pop = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(solved, correct) {
        if (solved && correct) {
            pop.animateTo(1.12f, tween(140))
            pop.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Box(
        modifier
            .offset { IntOffset(shake.value.roundToInt(), 0) }
            .scale(pop.value)
            .clip(RoundedCornerShape(26.dp))
            .background(BackgroundTint.Sand.color)
            .background(Feedback.Neutral.copy(alpha = 0.28f * wrongTint.value))
            .background(
                when {
                    solved && correct -> Feedback.Positive.copy(alpha = 0.22f)
                    // Wrong cards stay marked, so she can see what is already ruled out.
                    ruledOut -> Feedback.Wrong.copy(alpha = 0.22f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if ((solved && correct) || ruledOut) 5.dp else 0.dp,
                color = when {
                    solved && correct -> Feedback.Positive
                    ruledOut -> Feedback.Wrong
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(26.dp),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !solved,
            ) {
                if (correct) {
                    onCorrect()
                } else {
                    onWrong()
                    scope.launch { wobble(shake, wrongTint, shakeDistance) }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = choice.toString(),
                color = if (ruledOut) Ink.Primary.copy(alpha = 0.45f) else Ink.Primary,
                fontFamily = Armenian,
                fontWeight = FontWeight.Black,
                fontSize = glyphHeight.value.sp,
                // Armenian ascenders and descenders overflow the default line box, which is
                // what was clipping the numerals and the word beneath them.
                lineHeight = (glyphHeight.value * 1.05f).sp,
                maxLines = 1,
                softWrap = false,
            )
            Text(
                text = numberWord(choice),
                color = if (ruledOut) Ink.Primary.copy(alpha = 0.45f) else Ink.Primary,
                fontFamily = Armenian,
                fontWeight = FontWeight.Black,
                fontSize = (glyphHeight.value * 0.28f).sp,
                lineHeight = (glyphHeight.value * 0.40f).sp,
                maxLines = 1,
                softWrap = false,
            )
        }
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
