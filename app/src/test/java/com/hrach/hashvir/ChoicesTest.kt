package com.hrach.hashvir

import com.hrach.hashvir.game.choicesFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

class ChoicesTest {

    @Test
    fun `three distinct choices in range, always containing the answer`() {
        val random = Random(7)
        for (answer in 1..10) {
            repeat(200) {
                val choices = choicesFor(answer, random)
                assertEquals("$answer -> $choices", 3, choices.size)
                assertEquals("$answer -> $choices not distinct", 3, choices.toSet().size)
                assertTrue("$answer -> $choices missing the answer", answer in choices)
                assertTrue("$answer -> $choices out of range", choices.all { it in 1..10 })
            }
        }
    }

    @Test
    fun `distractors sit within three of the answer`() {
        val random = Random(8)
        for (answer in 1..10) {
            repeat(200) {
                val distractors = choicesFor(answer, random).filter { it != answer }
                assertTrue(
                    "$answer -> $distractors too far",
                    distractors.all { abs(it - answer) in 1..3 },
                )
            }
        }
    }

    @Test
    fun `the answer does not always sit in the same position`() {
        val random = Random(9)
        val positions = (1..300).map { choicesFor(5, random).indexOf(5) }.toSet()
        assertEquals(setOf(0, 1, 2), positions)
    }
}
