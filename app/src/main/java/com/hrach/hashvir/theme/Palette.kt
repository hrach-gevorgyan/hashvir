package com.hrach.hashvir.theme

import androidx.compose.ui.graphics.Color

/**
 * The island Պույ-պույ lives on. Warm sand, shallow sea, palm shade — the world of the tale
 * where she climbs into a coconut, eats until she cannot get out, and cries herself thin again.
 */
enum class BackgroundTint(val color: Color) {
    Sand(Color(0xFFFDF3E0)),
    Lagoon(Color(0xFFDDF1F2)),
    Palm(Color(0xFFE3F0DC)),
    Sunset(Color(0xFFFDE8D8)),
    Shell(Color(0xFFF7EDF3)),
}

/** Tropical set dressing. Muted on purpose: scenery, never the subject. */
object Island {
    val Sea = Color(0xFF5FBDBF)
    val SeaDeep = Color(0xFF3E9BA3)
    val SandDark = Color(0xFFE8CFA6)
    val PalmLeaf = Color(0xFF5C9E52)
    val PalmDark = Color(0xFF3E7A44)
    val Trunk = Color(0xFFA97B4F)
    val Sun = Color(0xFFFFD166)
    val CoconutShell = Color(0xFF8A5A3B)
    val CoconutDark = Color(0xFF6B4429)
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

        /** The size the outline width is quoted against: one fruit at the dp floor. */
        const val ReferenceDiameterDp = 126f
    }
}

/** One saturated colour per number, so each has an identity to remember it by. Index 0 unused. */
val NumberColors = listOf(
    Color(0xFF000000),
    Color(0xFFE03131), Color(0xFFF5901E), Color(0xFFF2B705), Color(0xFF94C11F),
    Color(0xFF3BA55C), Color(0xFF14A0A0), Color(0xFF2D7FC1), Color(0xFF5B5BD6),
    Color(0xFF7B3FA0), Color(0xFFE4356E),
)

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

    /** Marks a ruled-out card. Muted rather than alarming — it is information, not a telling-off. */
    val Wrong = Color(0xFFC94A4A)
}
