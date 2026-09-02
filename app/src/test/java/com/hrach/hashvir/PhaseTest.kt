package com.hrach.hashvir

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Mirrors GameViewModel.isRecognitionTurn, which cannot be unit tested directly because the
 * ViewModel needs an Application for SharedPreferences.
 */
private fun isRecognitionTurn(roundsCompleted: Int): Boolean {
    if (roundsCompleted < 30) return false
    return ((roundsCompleted - 30) / 3) % 2 == 1
}

class PhaseTest {

    @Test
    fun `recognition stays locked for the first thirty rounds`() {
        for (rounds in 0 until 30) {
            assertFalse("unlocked too early at $rounds", isRecognitionTurn(rounds))
        }
    }

    @Test
    fun `three counting then three recognition, repeating`() {
        val pattern = (30 until 42).map { if (isRecognitionTurn(it)) 'R' else 'C' }.joinToString("")
        assertEquals("CCCRRRCCCRRR", pattern)
    }
}
