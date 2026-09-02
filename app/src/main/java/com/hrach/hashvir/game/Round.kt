package com.hrach.hashvir.game

import androidx.compose.ui.geometry.Offset
import kotlin.random.Random
import com.hrach.hashvir.theme.BackgroundTint
import com.hrach.hashvir.theme.ObjectType

data class Round(
    val count: Int,
    val objectType: ObjectType,
    val background: BackgroundTint,
    /** Object centres, normalized 0f..1f within the play area. */
    val positions: List<Offset>,
    val tapped: Set<Int> = emptySet(),
) {
    val isComplete: Boolean get() = tapped.size == count
}

/**
 * A recognition round: Պույ-պույ asks for [answer], and it is one of three [choices].
 */
data class RecognitionRound(
    val answer: Int,
    val choices: List<Int>,
    val background: BackgroundTint,
) {
    init {
        require(answer in choices) { "the answer must be among the choices" }
    }
}

/**
 * Three choices containing [answer], in randomized order.
 *
 * Distractors come from answer +-1..3 so the discrimination is real — 3 against 8 teaches
 * nothing, 3 against 4 does. Clamped to 1..10 and always distinct.
 */
fun choicesFor(answer: Int, random: Random = Random.Default): List<Int> {
    val nearby = ((answer - 3)..(answer + 3))
        .filter { it != answer && it in 1..10 }
        .shuffled(random)
    return (nearby.take(2) + answer).shuffled(random)
}

/** The written word shown beneath the numeral. Index 0 is unused. */
private val NUMBER_WORDS = listOf(
    "",
    "\u0574\u0565\u056F",                     // մեկ
    "\u0565\u0580\u056F\u0578\u0582",         // երկու
    "\u0565\u0580\u0565\u0584",               // երեք
    "\u0579\u0578\u0580\u057D",               // չորս
    "\u0570\u056B\u0576\u0563",               // հինգ
    "\u057E\u0565\u0581",                     // վեց
    "\u0575\u0578\u0569",                     // յոթ
    "\u0578\u0582\u0569",                     // ութ
    "\u056B\u0576\u0568",                     // ինը
    "\u057F\u0561\u057D\u0568",               // տասը
)

fun numberWord(count: Int): String = NUMBER_WORDS[count]

/**
 * Round generation, kept pure so the no-repeat rules are testable without Android.
 *
 * All counts 1-10 from first launch: no ramp, no unlock. The count list to ten is already
 * familiar at three; what is being taught is quantity -> numeral -> word, which is no
 * harder at 8 than at 3.
 */
fun nextCount(previous: Int?, range: IntRange = 1..10, random: Random = Random.Default): Int {
    val choices = range.filter { it != previous }
    return choices[random.nextInt(choices.size)]
}

fun nextObjectType(previous: ObjectType?, random: Random = Random.Default): ObjectType {
    val choices = ObjectType.entries.filter { it != previous }
    return choices[random.nextInt(choices.size)]
}

/** Rotates rather than shuffles: novelty without the chance of the same tint twice running. */
fun nextBackground(previous: BackgroundTint?): BackgroundTint {
    val all = BackgroundTint.entries
    val index = previous?.let { all.indexOf(it) + 1 } ?: 0
    return all[index % all.size]
}
