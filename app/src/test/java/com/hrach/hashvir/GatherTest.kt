package com.hrach.hashvir

import androidx.compose.ui.unit.dp
import com.hrach.hashvir.game.Layout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class GatherTest {

    @Test
    fun `every counted fruit gets a place in the group`() {
        for (count in 1..10) {
            val places = Layout.gathered(count, 411.dp, 891.dp, 70.dp, centreY = 0.32f)
            assertEquals(count, places.size)
            assertTrue("count=$count off screen: $places", places.all { it.x in 0f..1f && it.y in 0f..1f })
        }
    }

    @Test
    fun `rows are centred and never wider than five`() {
        for (count in 1..10) {
            val places = Layout.gathered(count, 411.dp, 891.dp, 70.dp, centreY = 0.32f)
            val rows = places.groupBy { it.y }
            assertTrue("count=$count has a row wider than 5", rows.values.all { it.size <= 5 })
            for (row in rows.values) {
                val middle = (row.minOf { it.x } + row.maxOf { it.x }) / 2f
                assertTrue("count=$count row not centred at $middle", abs(middle - 0.5f) < 0.001f)
            }
        }
    }
}
