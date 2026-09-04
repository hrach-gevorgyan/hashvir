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
    fun `ordering counts stay in the three to five range and never repeat`() {
        val random = Random(11)
        var previous: Int? = null
        val seen = mutableSetOf<Int>()
        repeat(500) {
            val count = nextCount(previous, range = 3..5, random = random)
            assertTrue("count $count out of range", count in 3..5)
            assertTrue("count $count repeated", count != previous)
            seen += count
            previous = count
        }
        assertEquals(setOf(3, 4, 5), seen)
    }

    @Test
    fun `every number gets a distinct place that does not overlap another`() {
        for (count in 3..5) {
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
