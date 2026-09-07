package com.hrach.hashvir

import com.hrach.hashvir.audio.SoundBank
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * tools/words.csv drives clip generation. If a clip name is added to the app and not to the
 * list, regenerating the voice silently leaves that clip behind, and the app goes quiet in one
 * place without anything failing.
 */
class WordsCsvTest {

    private val nonVerbal = setOf("chime", "star_1", "star_2", "star_3")

    private fun keys(): Set<String> {
        val csv = File("../tools/words.csv")
        assertTrue("tools/words.csv not found at ${csv.absolutePath}", csv.exists())
        return csv.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .map { it.substringBefore(',').trim() }
            .toSet()
    }

    @Test
    fun `every spoken clip the app can ask for has a line in words csv`() {
        val spoken = SoundBank.ALL.filterNot { it in nonVerbal }.toSet()
        val listed = keys()
        // Praise slots beyond what is recorded are deliberately optional.
        val missing = (spoken - listed).filterNot { it.startsWith("praise_") }
        assertTrue("in SoundBank.ALL but not in words.csv: $missing", missing.isEmpty())
    }

    @Test
    fun `words csv lists nothing the app never plays`() {
        val extra = keys() - SoundBank.ALL.toSet()
        assertTrue("in words.csv but never played: $extra", extra.isEmpty())
    }
}
