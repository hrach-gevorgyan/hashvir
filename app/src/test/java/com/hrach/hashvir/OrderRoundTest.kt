package com.hrach.hashvir

import androidx.compose.ui.unit.dp
import com.hrach.hashvir.game.Layout
import com.hrach.hashvir.game.nextCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot
import kotlin.random.Random

class OrderRoundTest {

    @Test
    fun `ordering counts cover three to ten and never repeat`() {
        val random = Random(11)
        var previous: Int? = null
        val seen = mutableSetOf<Int>()
        repeat(2000) {
            val count = nextCount(previous, range = 3..10, random = random)
            assertTrue("count $count out of range", count in 3..10)
            assertTrue("count $count repeated", count != previous)
            seen += count
            previous = count
        }
        assertEquals((3..10).toSet(), seen)
    }

    /**
     * Layout hands back places in reading order. If the round used them in that order the
     * numbers would climb across the screen and she could finish without reading any of them.
     */
    @Test
    fun `shuffling breaks the reading order that placement comes back in`() {
        var sameAsPlacement = 0
        for (seed in 0 until 200) {
            val random = Random(seed)
            val places = Layout.positions(5, 411.dp, Layout.playHeight(891.dp), true, random)
            if (places.shuffled(random) == places) sameAsPlacement += 1
        }
        assertTrue("shuffle left the order untouched $sameAsPlacement times", sameAsPlacement < 10)
    }

    @Test
    fun `every number gets a distinct place that does not overlap another`() {
        for (count in 3..10) {
            for (seed in 0 until 40) {
                val width = 411.dp
                val height = Layout.playHeight(891.dp)
                val d = Layout.diameter(count, width, height, compact = true)
                val places = Layout.positions(count, width, height, compact = true, Random(seed))

                assertEquals(count, places.size)
                for (i in places.indices) {
                    for (j in i + 1 until places.size) {
                        val dx = (places[i].x - places[j].x) * width.value
                        val dy = (places[i].y - places[j].y) * height.value
                        assertTrue(
                            "count=$count seed=$seed numbers overlap",
                            hypot(dx, dy) >= d.value - 0.5f,
                        )
                    }
                }
            }
        }
    }
}
