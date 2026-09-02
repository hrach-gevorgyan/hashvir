package com.hrach.hashvir.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/** WCAG 2.1 relative luminance. */
private fun luminance(c: Color): Double {
    fun channel(v: Float): Double {
        val d = v.toDouble()
        return if (d <= 0.03928) d / 12.92 else ((d + 0.055) / 1.055).pow(2.4)
    }
    return 0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)
}

fun contrastRatio(a: Color, b: Color): Double {
    val la = luminance(a)
    val lb = luminance(b)
    return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
}

/**
 * The colour that has to hold the object's edge against the background. Objects are read
 * by their outline, so that is what must clear 4.5:1.
 */
val Fruit.edgeColor: Color get() = outline

fun Fruit.contrastOn(tint: BackgroundTint): Double = contrastRatio(edgeColor, tint.color)

