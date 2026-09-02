package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.Text
import com.hrach.hashvir.game.numberWord
import com.hrach.hashvir.theme.Armenian
import com.hrach.hashvir.theme.Ink

/**
 * Numeral and Armenian word as a single unit — one scale, one colour, one animation.
 * Quantity, numeral, written word and spoken word land together; that mapping is the
 * whole point of the round end.
 */
@Composable
fun NumberGlyph(count: Int, glyphHeight: Dp, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(count) {
        scale.snapTo(0f)
        scale.animateTo(1f, tween(durationMillis = 400, easing = FastOutSlowInEasing))
    }

    val density = LocalDensity.current
    val glyphSize = with(density) { glyphHeight.toSp() }
    val wordSize = with(density) { (glyphHeight * 0.25f).toSp() }

    Column(
        modifier.scale(scale.value),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = count.toString(),
            color = Ink.Primary,
            fontFamily = Armenian,
            fontWeight = FontWeight.Black,
            fontSize = glyphSize,
            lineHeight = glyphSize * 1.05f,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
        Text(
            text = numberWord(count),
            color = Ink.Primary,
            fontFamily = Armenian,
            fontWeight = FontWeight.Black,
            fontSize = wordSize,
            lineHeight = wordSize * 1.4f,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
        )
    }
}
