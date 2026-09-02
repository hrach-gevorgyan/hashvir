package com.hrach.hashvir

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hrach.hashvir.game.Layout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random

/** Play areas the app has to work in, in dp. */
private data class Screen(val label: String, val width: Dp, val height: Dp, val compact: Boolean)

private val SCREENS = listOf(
    Screen("small phone", 360.dp, 640.dp, compact = true),
    Screen("pixel phone", 411.dp, 790.dp, compact = true),
    Screen("7in tablet", 600.dp, 900.dp, compact = false),
    Screen("11in tablet", 800.dp, 1180.dp, compact = false),
)

class LayoutTest {

    @Test
    fun `scattered objects never overlap and stay in bounds`() {
        for (screen in SCREENS) {
            for (count in 1..5) {
                for (seed in 0 until 50) {
                    val d = Layout.diameter(count, screen.width, screen.height, screen.compact)
                    val positions = Layout.positions(
                        count, screen.width, screen.height, screen.compact, Random(seed)
                    )
                    assertEquals(count, positions.size)

                    val halfX = d / 2f / screen.width
                    val halfY = d / 2f / screen.height
                    for (p in positions) {
                        assertTrue(
                            "${screen.label} count=$count seed=$seed out of bounds: $p",
                            p.x - halfX >= -0.001f && p.x + halfX <= 1.001f &&
                                p.y - halfY >= -0.001f && p.y + halfY <= 1.001f,
                        )
                    }

                    for (i in positions.indices) {
                        for (j in i + 1 until positions.size) {
                            val dx = (positions[i].x - positions[j].x) * screen.width.value
                            val dy = (positions[i].y - positions[j].y) * screen.height.value
                            val gap = hypot(dx, dy)
                            assertTrue(
                                "${screen.label} count=$count seed=$seed centres ${"%.1f".format(gap)}dp apart, need ${d.value}",
                                gap >= d.value - 0.5f,
                            )
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `structured rows split as specified`() {
        assertEquals(listOf(3, 3), Layout.rows(6, compact = false))
        assertEquals(listOf(4, 3), Layout.rows(7, compact = false))
        assertEquals(listOf(4, 4), Layout.rows(8, compact = false))
        assertEquals(listOf(3, 3, 3), Layout.rows(9, compact = false))
        assertEquals(listOf(5, 5), Layout.rows(10, compact = false))
        assertEquals(listOf(4, 3, 3), Layout.rows(10, compact = true))
        for (count in 6..10) {
            assertEquals(count, Layout.rows(count, compact = true).sum())
            assertEquals(count, Layout.rows(count, compact = false).sum())
        }
    }

    @Test
    fun `structured rows are evenly spaced and read left to right`() {
        for (screen in SCREENS) {
            for (count in 6..10) {
                val positions =
                    Layout.positions(count, screen.width, screen.height, screen.compact)
                assertEquals(count, positions.size)

                var index = 0
                val rowYs = mutableListOf<Float>()
                for (cols in Layout.rowsThatFit(count, screen.width, screen.height, screen.compact)) {
                    val row = positions.subList(index, index + cols)
                    index += cols

                    // one y per row
                    assertTrue(row.all { abs(it.y - row[0].y) < 0.001f })
                    rowYs += row[0].y

                    // strictly increasing x, evenly spaced
                    val gaps = row.zipWithNext { a, b -> b.x - a.x }
                    assertTrue("${screen.label} count=$count not left to right", gaps.all { it > 0f })
                    assertTrue(
                        "${screen.label} count=$count uneven columns: $gaps",
                        gaps.all { abs(it - gaps[0]) < 0.001f },
                    )

                    // centred horizontally
                    assertTrue(abs((row.first().x + row.last().x) / 2f - 0.5f) < 0.001f)
                }

                val rowGaps = rowYs.zipWithNext { a, b -> b - a }
                assertTrue("${screen.label} count=$count not top to bottom", rowGaps.all { it > 0f })
                assertTrue(
                    "${screen.label} count=$count uneven rows: $rowGaps",
                    rowGaps.all { abs(it - rowGaps[0]) < 0.001f },
                )
            }
        }
    }

    @Test
    fun `computed target size on every reference screen`() {
        val shortfalls = mutableListOf<String>()
        for (screen in SCREENS) {
            val row = (1..10).joinToString(" ") { count ->
                val d = Layout.diameter(count, screen.width, screen.height, screen.compact)
                if (d < Layout.PhoneFloor) {
                    shortfalls += "${screen.label} count=$count ${"%.0f".format(d.value)}dp"
                }
                "$count=${"%.0f".format(d.value)}"
            }
            // dp is defined as 1/160 inch, so dp -> mm is exact and density-independent.
            val mm = (1..10).joinToString(" ") { count ->
                val d = Layout.diameter(count, screen.width, screen.height, screen.compact)
                "%.0f".format(d.value * 25.4f / 160f)
            }
            println("${screen.label.padEnd(12)} $row")
            println("${" ".repeat(12)} mm: $mm")
        }
        println("below 113dp: $shortfalls")
        for (screen in SCREENS.filterNot { it.compact }) {
            for (count in 1..10) {
                val d = Layout.diameter(count, screen.width, screen.height, screen.compact)
                assertTrue(
                    "${screen.label} count=$count is ${"%.0f".format(d.value)}dp, below the 126dp floor",
                    d >= Layout.Floor,
                )
            }
        }
    }
}
