package com.hrach.hashvir.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.hrach.hashvir.theme.Ink

/**
 * Back to the menu. Low contrast and out of the way — findable by an adult, not an invitation
 * to a child mid-round.
 */
@Composable
fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Canvas(
        modifier
            .size(52.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(Ink.Soft.copy(alpha = 0.10f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onBack,
            )
            .padding(14.dp)
    ) {
        val w = size.width
        val h = size.height
        val arrow = Path().apply {
            moveTo(w * 0.62f, h * 0.12f)
            lineTo(w * 0.24f, h * 0.5f)
            lineTo(w * 0.62f, h * 0.88f)
        }
        drawPath(
            arrow,
            Ink.Soft.copy(alpha = 0.55f),
            style = Stroke(width = w * 0.16f, cap = StrokeCap.Round),
        )
        drawCircle(Ink.Soft.copy(alpha = 0f), radius = 0f, center = Offset.Zero)
    }
}
