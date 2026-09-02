package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrach.hashvir.theme.Armenian
import com.hrach.hashvir.theme.BackgroundTint
import com.hrach.hashvir.theme.Fruit
import com.hrach.hashvir.theme.Ink
import kotlinx.coroutines.launch

enum class Mode(val label: String, val tint: Color) {
    /** Հաշվել — count the fruit. */
    Count("Հաշվել", Color(0xFFFFE3C7)),

    /** Գուշակել — pick the number she names. */
    Guess("Գուշակել", Color(0xFFD9EAF7)),

    /** Սովորել — say the number out loud. */
    Learn("Սովորել", Color(0xFFDCEFDA)),
}

/**
 * Three ways in. Every card is a full-width target with a picture and one Armenian word, so
 * an adult can read it and a child can learn to recognise the shape of each one.
 */
@Composable
fun MenuScreen(onPick: (Mode) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(BackgroundTint.Paper.color)
    ) {
        val cardHeight = maxOf((maxHeight - 220.dp) / 3f, 140.dp)

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            for (mode in Mode.entries) {
                MenuCard(mode = mode, height = cardHeight, onPick = onPick)
            }
        }

        PouyPouy(
            state = HelperState.Suggesting,
            size = maxHeight * 0.13f,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
        )
    }
}

@Composable
private fun MenuCard(mode: Mode, height: Dp, onPick: (Mode) -> Unit) {
    val press = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Row(
        Modifier
            .fillMaxWidth()
            .height(height)
            .scale(press.value)
            .clip(RoundedCornerShape(28.dp))
            .background(mode.tint)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                scope.launch {
                    press.animateTo(0.95f, tween(90))
                    press.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                    onPick(mode)
                }
            }
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ModeIcon(mode = mode, size = height * 0.52f)
        Text(
            text = mode.label,
            color = Ink.Primary,
            fontFamily = Armenian,
            fontWeight = FontWeight.Black,
            // Գուշակել is the longest word; at any larger size it wraps mid-word.
            fontSize = (height.value * 0.145f).sp,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/** A picture per mode, so the cards are told apart before any of them can be read. */
@Composable
private fun ModeIcon(mode: Mode, size: Dp) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) { enter.animateTo(1f, tween(320, easing = FastOutSlowInEasing)) }

    Box(
        Modifier
            .size(size)
            .scale(enter.value),
        contentAlignment = Alignment.Center,
    ) {
        when (mode) {
            // Three apples: exactly what she will be counting.
            Mode.Count -> Canvas(Modifier.fillMaxSize()) {
                val box = this.size.minDimension
                drawFruit(Fruit.Apple, box * 0.62f, Offset(0f, box * 0.30f))
                drawFruit(Fruit.Apple, box * 0.62f, Offset(box * 0.38f, box * 0.30f))
                drawFruit(Fruit.Apple, box * 0.62f, Offset(box * 0.19f, 0f))
            }

            // Four cards with one picked out — the shape of the Գուշակել screen itself.
            Mode.Guess -> ChoiceGrid(size)

            // One number being spoken: the numeral with a speech bubble.
            Mode.Learn -> SpokenNumber(size)
        }
    }
}

/** A miniature of the four-card grid, with the chosen one lifted and green. */
@Composable
private fun ChoiceGrid(size: Dp) {
    val cell = size * 0.42f
    Column(
        Modifier.size(size),
        verticalArrangement = Arrangement.spacedBy(size * 0.08f, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (row in listOf(listOf("2", "7"), listOf("4", "9"))) {
            Row(horizontalArrangement = Arrangement.spacedBy(size * 0.08f)) {
                for (numeral in row) {
                    val picked = numeral == "7"
                    Box(
                        Modifier
                            .size(cell)
                            .clip(RoundedCornerShape(percent = 26))
                            .background(if (picked) Color(0xFF3BA55C) else Color(0xFFFFFFFF)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = numeral,
                            color = if (picked) Color.White else Ink.Primary,
                            fontFamily = Armenian,
                            fontWeight = FontWeight.Black,
                            fontSize = (cell.value * 0.62f).sp,
                        )
                    }
                }
            }
        }
    }
}

/** A numeral with a speech bubble: this is the mode where she says it out loud. */
@Composable
private fun SpokenNumber(size: Dp) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Text(
            text = "5",
            color = Color(0xFF7B3FA0),
            fontFamily = Armenian,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.78f).sp,
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(size * 0.44f)
                .clip(
                    RoundedCornerShape(
                        topStartPercent = 45,
                        topEndPercent = 45,
                        bottomEndPercent = 45,
                        bottomStartPercent = 8,
                    )
                )
                .background(Color(0xFF3BA55C)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "•••",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.15f).sp,
            )
        }
    }
}
