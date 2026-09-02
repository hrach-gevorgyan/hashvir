package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.audio.rememberSpeaking
import com.hrach.hashvir.game.nextCount
import com.hrach.hashvir.game.numberWord
import com.hrach.hashvir.theme.Armenian
import com.hrach.hashvir.theme.BackgroundTint
import com.hrach.hashvir.theme.Ink
import kotlinx.coroutines.delay
import kotlin.random.Random

/** One saturated colour per number, so each one has its own identity to remember it by. */
private val NUMBER_COLORS = listOf(
    Color(0xFFE03131), Color(0xFFF5901E), Color(0xFFF2B705), Color(0xFF94C11F),
    Color(0xFF3BA55C), Color(0xFF14A0A0), Color(0xFF2D7FC1), Color(0xFF5B5BD6),
    Color(0xFF7B3FA0), Color(0xFFE4356E),
)

/** praise runs to 1.04s and oops to 0.85s; nothing here may overlap anything else. */
private const val AFTER_PRAISE_MS = 1400L
private const val AFTER_OOPS_MS = 1100L

/**
 * Սովորել — Պույ-պույ shows a number and asks what it is. The child says it out loud, and the
 * grown-up sitting next to her presses green or red.
 *
 * Green: she was right. Պույ-պույ hops, and the next number comes up.
 * Red: not yet. Պույ-պույ shakes her head, the number is spoken aloud, and it stays on screen
 * so she can try the same one again. Nothing is scored and nothing is taken away.
 */
@Composable
fun LearnScreen(sounds: SoundBank, onBack: () -> Unit, modifier: Modifier = Modifier) {
    var number by remember { mutableIntStateOf(Random.nextInt(1, 11)) }
    var reaction by remember { mutableStateOf<Boolean?>(null) }
    var asking by remember { mutableIntStateOf(0) }

    // Ask whenever a new number appears, or after a wrong answer on the same number.
    LaunchedEffect(number, asking) {
        reaction = null
        delay(250)
        sounds.play("what_number")
    }

    LaunchedEffect(reaction) {
        when (reaction) {
            true -> {
                sounds.play("praise_${Random.nextInt(1, 5)}")
                delay(AFTER_PRAISE_MS)
                // Random, never the same number twice running.
                number = nextCount(number)
            }

            false -> {
                // Just the soft "not yet" and a head shake. The number stays on screen and
                // she gets another go; saying the answer for her would defeat the point.
                sounds.play("oops_${Random.nextInt(1, 3)}")
                delay(AFTER_OOPS_MS)
                asking += 1
            }

            null -> Unit
        }
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(BackgroundTint.Sand.color)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        val shorter = minOf(maxWidth, maxHeight)
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Scenery(seed = number, strength = 0.35f)

        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            BigNumber(number = number, height = shorter * 0.70f)

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            ) {
                JudgeButton(
                    color = Color(0xFF3BA55C),
                    mark = Mark.Tick,
                    size = shorter * 0.26f,
                    enabled = reaction == null,
                    onClick = { reaction = true },
                )
                JudgeButton(
                    color = Color(0xFFC94A4A),
                    mark = Mark.Cross,
                    size = shorter * 0.26f,
                    enabled = reaction == null,
                    onClick = { reaction = false },
                )
            }
        }

        if (reaction == true) {
            Confetti(
                origin = with(LocalDensity.current) {
                    Offset((screenWidth / 2f).toPx(), (screenHeight * 0.38f).toPx())
                }
            )
        }

        // Top-right, clear of the two judge buttons at the bottom.
        PouyPouy(
            state = when (reaction) {
                true -> HelperState.Happy
                false -> HelperState.Sad
                null -> HelperState.Thinking
            },
            size = shorter * 0.32f,
            speaking = rememberSpeaking(sounds),
            modifier = Modifier
                .align(Alignment.TopEnd)
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

/** The number, big and in its own colour, with the Armenian word beneath it. */
@Composable
private fun BigNumber(number: Int, height: Dp) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(number) {
        enter.snapTo(0f)
        enter.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }

    Column(
        Modifier.scale(enter.value),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = number.toString(),
            color = NUMBER_COLORS[number - 1],
            fontFamily = Armenian,
            fontWeight = FontWeight.Black,
            fontSize = height.value.sp,
            lineHeight = (height.value * 1.05f).sp,
            maxLines = 1,
        )
        Text(
            text = numberWord(number),
            color = Ink.Primary,
            fontFamily = Armenian,
            fontWeight = FontWeight.Black,
            fontSize = (height.value * 0.22f).sp,
            lineHeight = (height.value * 0.30f).sp,
            maxLines = 1,
            softWrap = false,
        )
    }
}

private enum class Mark { Tick, Cross }

/**
 * The grown-up's control, not the child's. Deliberately plain: a big circle with a tick or a
 * cross, no label, nothing that reads as part of the game.
 */
@Composable
private fun JudgeButton(
    color: Color,
    mark: Mark,
    size: Dp,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val press = remember { Animatable(1f) }

    Box(
        Modifier
            .size(size)
            .scale(press.value)
            .clip(RoundedCornerShape(percent = 50))
            .background(if (enabled) color else color.copy(alpha = 0.35f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (mark == Mark.Tick) "✓" else "✕",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.44f).sp,
        )
    }

    LaunchedEffect(enabled) {
        if (enabled) press.animateTo(1f, tween(150, easing = FastOutSlowInEasing))
    }
}
