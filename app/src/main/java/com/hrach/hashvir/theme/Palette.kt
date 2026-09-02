package com.hrach.hashvir.theme

import androidx.compose.ui.graphics.Color

/** Pastel ground. Rotates per round for novelty without clutter. */
enum class BackgroundTint(val color: Color) {
    Paper(Color(0xFFFBF6EE)),
    Sage(Color(0xFFE6EFE4)),
    Sky(Color(0xFFE4EDF6)),
    Peach(Color(0xFFFBEBE1)),
    Lilac(Color(0xFFF0E8F4)),
}

/**
 * Saturated figure. Always the most vivid layer on screen.
 *
 * Every object carries the same 3dp outline. Saturated fills cannot clear 4.5:1 against
 * pastel grounds (apricot reaches only 2.2:1, star 1.5:1), so the outline is what holds
 * the edge and what contrast is measured on. Fills stay fully saturated.
 */
enum class ObjectType(val color: Color) {
    Apricot(Color(0xFFF5901E)),
    Pomegranate(Color(0xFFD42B3A)),
    Grape(Color(0xFF7B3FA0)),
    Balloon(Color(0xFFE4356E)),
    Star(Color(0xFFF5C518)),
    Sheep(Color(0xFFFFFFFF)),
    ;

    val outline: Color get() = OutlineColor

    companion object {
        val OutlineColor = Color(0xFF3A4454)
        const val OutlineWidthDp = 3f
    }
}

object Ink {
    val Primary = Color(0xFF2E2A28)
    val Soft = Color(0xFF6B6560)
}

object Mouse {
    val Body = Color(0xFFB9B3C7)
    val Ear = Color(0xFFF2C4C9)
    val Detail = Color(0xFF3A4454)
}

object Feedback {
    val Positive = Color(0xFF3BA55C)
    val Neutral = Color(0xFFC9A227)
}
