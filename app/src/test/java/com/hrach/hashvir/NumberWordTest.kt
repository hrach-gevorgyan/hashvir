package com.hrach.hashvir

import com.hrach.hashvir.game.numberWord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberWordTest {

    @Test
    fun `every count 1 to 10 has a distinct non-empty word`() {
        val words = (1..10).map { numberWord(it) }
        assertTrue("blank word at ${words.indexOf("") + 1}", words.none { it.isBlank() })
        assertEquals(10, words.toSet().size)
    }

    @Test
    fun `words are armenian`() {
        val armenian = Regex("[\u0530-\u058F]+")
        val bad = (1..10).filterNot { armenian.matches(numberWord(it)) }
        assertTrue("not armenian: $bad", bad.isEmpty())
    }
}
