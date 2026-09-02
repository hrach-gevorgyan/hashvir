package com.hrach.hashvir

import com.hrach.hashvir.theme.BackgroundTint
import com.hrach.hashvir.theme.Ink
import com.hrach.hashvir.theme.ObjectType
import com.hrach.hashvir.theme.contrastOn
import com.hrach.hashvir.theme.contrastRatio
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {

    @Test
    fun `ink primary clears 7 to 1 on every background`() {
        val failures = BackgroundTint.entries
            .map { it.name to contrastRatio(Ink.Primary, it.color) }
            .filter { it.second < 7.0 }
        assertTrue("ink-primary below 7:1 on $failures", failures.isEmpty())
    }

/** All 6 x 5 pairings, measured on the outline that holds each object's edge. */
    @Test
    fun `every object clears 4 point 5 to 1 on every background`() {
        val failures = mutableListOf<String>()
        for (obj in ObjectType.entries) {
            val row = BackgroundTint.entries.joinToString {
                "${it.name}=${"%.2f".format(obj.contrastOn(it))}"
            }
            println("${obj.name}: $row")
            BackgroundTint.entries
                .filter { obj.contrastOn(it) < 4.5 }
                .forEach { failures += "${obj.name}/${it.name}" }
        }
        assertTrue("below 4.5:1: $failures", failures.isEmpty())
    }
}
