package com.hrach.hashvir.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import com.hrach.hashvir.theme.Fruit

private const val TAPPED_SCALE = 0.85f
private const val TAPPED_ALPHA = 0.45f

/**
 * One tappable fruit.
 *
 * A tapped fruit stays on screen at reduced scale and opacity for the rest of the round — at
 * high counts that is what lets her see what is left without recounting from the start.
 */
@Composable
fun FruitSprite(
    fruit: Fruit,
    diameter: Dp,
    tapped: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    dimWhenTapped: Boolean = true,
) {
    val scale = remember { Animatable(1f) }
    val wiggle = remember { Animatable(0f) }

    LaunchedEffect(tapped) {
        if (tapped) {
            scale.animateTo(1.25f, tween(durationMillis = 100))
            scale.animateTo(
                TAPPED_SCALE,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            )
        } else {
            // Sprites are reused across rounds; without this a fresh fruit inherits the
            // previous round's tapped scale.
            scale.snapTo(1f)
            wiggle.snapTo(0f)
        }
    }

    LaunchedEffect(tapped) {
        if (tapped) {
            wiggle.animateTo(-8f, tween(90))
            wiggle.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Canvas(
        modifier
            .size(diameter)
            .scale(if (tapped && !dimWhenTapped) 1f else scale.value)
            .rotate(wiggle.value)
            .alpha(if (tapped && dimWhenTapped) TAPPED_ALPHA else 1f)
            // No ripple: the bounce is the response, and a ripple would out-compete the fill.
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = !tapped,
                onClick = onTap,
            )
    ) {
        drawFruit(fruit, size.minDimension)
    }
}
