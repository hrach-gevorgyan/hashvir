package com.hrach.hashvir

import com.hrach.hashvir.audio.SoundBank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundBankTest {

    @Test
    fun `clip list matches the clips in AUDIO md`() {
        assertEquals(43, SoundBank.ALL.size)
        assertEquals(SoundBank.ALL.size, SoundBank.ALL.toSet().size)
    }

    @Test
    fun `clip names are valid raw resource names`() {
        val valid = Regex("[a-z][a-z0-9_]*")
        val bad = SoundBank.ALL.filterNot { valid.matches(it) }
        assertTrue("invalid raw resource names: $bad", bad.isEmpty())
    }
}
