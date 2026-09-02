package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hrach.hashvir.theme.ObjectType

private const val TAPPED_SCALE = 0.85f
private const val TAPPED_ALPHA = 0.45f

/**
 * A countable object. Saturated fill, 3dp outline so the edge holds against the pastel ground.
 *
 * A tapped object stays on screen at reduced scale and opacity for the rest of the round —
 * at counts 9-10 that is what lets her see what is left without recounting from the start.
 */
@Composable
fun ObjectSprite(
    type: ObjectType,
    diameter: Dp,
    tapped: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale = remember { Animatable(1f) }

    // Bounce out and settle: 1.0 -> 1.25 -> resting, inside 300ms.
    LaunchedEffect(tapped) {
        if (tapped) {
            scale.animateTo(1.25f, tween(durationMillis = 100))
            scale.animateTo(
                TAPPED_SCALE,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            )
        } else {
            // Sprites are reused across rounds; without this a fresh object inherits the
            // previous round's tapped scale.
            scale.snapTo(1f)
        }
    }

    Canvas(
        modifier
            .size(diameter)
            .scale(scale.value)
            .alpha(if (tapped) TAPPED_ALPHA else 1f)
            // No ripple: the bounce is the response, and a ripple would out-compete the fill.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !tapped,
                onClick = onTap,
            )
    ) {
        val stroke = ObjectType.OutlineWidthDp.dp.toPx()
        val radius = size.minDimension / 2f
        drawCircle(type.color, radius = radius)
        drawCircle(type.outline, radius = radius - stroke / 2f, style = Stroke(width = stroke))
    }
}
