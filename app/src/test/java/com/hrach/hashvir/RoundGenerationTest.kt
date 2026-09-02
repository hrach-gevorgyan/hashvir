package com.hrach.hashvir

import com.hrach.hashvir.game.nextBackground
import com.hrach.hashvir.game.nextCount
import com.hrach.hashvir.game.nextFruit
import com.hrach.hashvir.theme.BackgroundTint
import com.hrach.hashvir.theme.Fruit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class RoundGenerationTest {

    @Test
    fun `count never repeats and stays in range`() {
        val random = Random(1)
        var previous: Int? = null
        val seen = mutableSetOf<Int>()
        repeat(2000) {
            val count = nextCount(previous, random = random)
            assertTrue("count $count out of range", count in 1..10)
            assertTrue("count $count repeated", count != previous)
            seen += count
            previous = count
        }
        assertEquals("every count 1..10 should appear", (1..10).toSet(), seen)
    }

    @Test
    fun `count honours a restricted range`() {
        val random = Random(2)
        var previous: Int? = null
        repeat(500) {
            val count = nextCount(previous, range = 1..5, random = random)
            assertTrue(count in 1..5)
            assertTrue(count != previous)
            previous = count
        }
    }

    @Test
    fun `fruit never repeats consecutively`() {
        val random = Random(3)
        var previous: Fruit? = null
        val seen = mutableSetOf<Fruit>()
        repeat(2000) {
            val type = nextFruit(previous, random)
            assertTrue("type repeated", type != previous)
            seen += type
            previous = type
        }
        assertEquals(Fruit.entries.toSet(), seen)
    }

    @Test
    fun `background rotates through every tint in order`() {
        var tint = nextBackground(null)
        assertEquals(BackgroundTint.entries.first(), tint)
        val order = mutableListOf(tint)
        repeat(BackgroundTint.entries.size - 1) {
            tint = nextBackground(tint)
            order += tint
        }
        assertEquals(BackgroundTint.entries.toList(), order)
        assertEquals(BackgroundTint.entries.first(), nextBackground(tint))
    }
}
