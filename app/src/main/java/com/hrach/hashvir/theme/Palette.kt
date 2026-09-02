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
 * The countable things: real fruit, fully saturated. Always the most vivid layer on screen.
 *
 * Every fruit carries the same 3dp outline. Saturated fills cannot clear 4.5:1 against pastel
 * grounds, so the outline is what holds the edge and what contrast is measured on.
 */
enum class Fruit(val color: Color, val detail: Color) {
    Apple(Color(0xFFE03131), Color(0xFF2F9E44)),
    Orange(Color(0xFFF5901E), Color(0xFF2F9E44)),
    Banana(Color(0xFFF2B705), Color(0xFF8A6D3B)),
    Pear(Color(0xFF94C11F), Color(0xFF6B8E13)),
    Strawberry(Color(0xFFE8352E), Color(0xFF2F9E44)),
    Grapes(Color(0xFF7B3FA0), Color(0xFF2F9E44)),
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
