package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.theme.ObjectType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.launch

/** Always three, on every completed round, regardless of speed, mistakes or count. */
private const val STAR_COUNT = 3

/**
 * Punctuation, not currency.
 *
 * There is deliberately no total, no bank, no persistence and no counter. Each star is a toy
 * attached to finishing — tapping one plays an ascending note and spins it, and does not
 * advance or delay the round.
 */
@Composable
fun Stars(sounds: SoundBank, starSize: Dp, modifier: Modifier = Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(starSize * 0.35f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(STAR_COUNT) { index ->
            TappableStar(index = index, sounds = sounds, starSize = starSize)
        }
    }
}

@Composable
private fun TappableStar(index: Int, sounds: SoundBank, starSize: Dp) {
    val spin = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Canvas(
        Modifier
            .size(starSize)
            .rotate(spin.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                sounds.play("star_${index + 1}")
                scope.launch {
                    spin.snapTo(0f)
                    spin.animateTo(360f, tween(durationMillis = 400, easing = FastOutSlowInEasing))
                }
            }
    ) {
        val path = starPath(size.minDimension / 2f, center = Offset(size.width / 2f, size.height / 2f))
        val stroke = ObjectType.OutlineWidthDp.dp.toPx()
        drawPath(path, ObjectType.Star.color)
        drawPath(path, ObjectType.Star.outline, style = Stroke(width = stroke))
    }
}

private fun starPath(radius: Float, center: Offset): Path = Path().apply {
    val points = 5
    val inner = radius * 0.45f
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius - 1.5f else inner
        val angle = (PI * i / points - PI / 2).toFloat()
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
