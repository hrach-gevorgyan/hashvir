package com.hrach.hashvir.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor
import kotlin.random.Random

/**
 * Placement and sizing for the play area.
 *
 * Two modes, by design: scattered at 1-5 is play, structured rows at 6-10 is countable.
 * Do not scatter at high counts — she loses track of which ones she has already had.
 *
 * Pure and framework-free so every rule here is unit-testable.
 */
object Layout {

    /** ~20mm. Nielsen Norman's minimum for young children — four times the adult minimum. */
    val Floor = 126.dp

    /** ~18mm. Counts 9-10 on a phone only; a phone cannot fit 20mm in the required columns. */
    val PhoneFloor = 113.dp

    /** Gap between adjacent objects, as a fraction of diameter. Crowding defeats big targets. */
    const val SpacingRatio = 0.25f

    /**
     * The strip along the bottom that Պույ-պույ stands in, as a fraction of screen height.
     *
     * Fruit are never placed in it. She is drawn over the play area, and on a wide screen the
     * rows reach far enough down that fruit ended up behind her; a tall phone hid the problem
     * because the same rows sit higher up.
     */
    private const val HelperBand = 0.19f

    /** The area fruit may actually occupy: everything above Պույ-պույ. */
    fun playHeight(height: Dp): Dp = height * (1f - HelperBand)

    /** Centre-to-centre distance for touching-but-not-crowded objects. */
    private fun pitch(diameter: Dp): Dp = diameter * (1f + SpacingRatio)

    /**
     * A tidy cluster for the cardinality question.
     *
     * Once the counting is done the fruit are pulled together into even rows, so she sees the
     * set as one group with one answer rather than as things that were touched in turn.
     */
    fun gathered(count: Int, width: Dp, height: Dp, diameter: Dp, centreY: Float): List<Offset> {
        val columns = minOf(count, 5)
        val rowSizes = buildList {
            var left = count
            while (left > 0) {
                val take = minOf(columns, left)
                add(take)
                left -= take
            }
        }
        val p = diameter * 1.15f
        val blockHeight = p * (rowSizes.size - 1)
        val top = height * centreY - blockHeight / 2f

        return buildList {
            rowSizes.forEachIndexed { rowIndex, inRow ->
                val rowWidth = p * (inRow - 1)
                val left = (width - rowWidth) / 2f
                val y = top + p * rowIndex
                repeat(inRow) { column ->
                    add(Offset((left + p * column) / width, y / height))
                }
            }
        }
    }

    /**
     * Row splits for the structured counts, as specified. 10 is the only one that differs
     * by device. Use [rowsThatFit] for placement — a narrow tablet cannot hold 5 columns.
     */
    fun rows(count: Int, compact: Boolean): List<Int> = when (count) {
        6 -> listOf(3, 3)
        7 -> listOf(4, 3)
        8 -> listOf(4, 4)
        9 -> listOf(3, 3, 3)
        10 -> if (compact) listOf(4, 3, 3) else listOf(5, 5)
        else -> error("structured layout is for counts 6..10, got $count")
    }

    /**
     * The split actually used. 5+5 needs 756dp of width to hold the floor, which a 7"
     * tablet does not have, so it falls back to the phone split rather than to 100dp targets.
     */
    fun rowsThatFit(count: Int, width: Dp, height: Dp, compact: Boolean): List<Int> {
        val spec = rows(count, compact)
        if (count != 10 || compact) return spec
        return if (fitFor(spec, width, height) >= Floor) spec else rows(count, compact = true)
    }

    fun floorFor(count: Int, compact: Boolean): Dp =
        if (compact && count >= 9) PhoneFloor else Floor

    /**
     * Diameter for [count] objects in a [width] x [height] play area.
     *
     * The percentage from SPEC is a floor, not a target: `max(percentage, dpFloor)`, then
     * capped by what actually fits. On phones the cap usually wins at high counts, which is
     * what CountingScreen logs a warning about.
     */
    fun diameter(count: Int, width: Dp, height: Dp, compact: Boolean): Dp {
        val shorter = minOf(width, height)
        val desired = when {
            count <= 3 -> shorter * 0.30f
            count <= 5 -> shorter * 0.24f
            else -> Dp(Float.MAX_VALUE) // structured size comes entirely from the fit
        }
        val wanted = maxOf(desired, floorFor(count, compact))
        val fits = if (count <= 5) {
            scatterFit(count, width, height, wanted, floorFor(count, compact))
        } else {
            structuredFit(count, width, height, compact)
        }
        return minOf(wanted, fits)
    }

    /** Largest diameter whose row/column structure fits, with spacing, in the play area. */
    private fun structuredFit(count: Int, width: Dp, height: Dp, compact: Boolean): Dp =
        fitFor(rowsThatFit(count, width, height, compact), width, height)

    private fun fitFor(rows: List<Int>, width: Dp, height: Dp): Dp {
        // n items with SpacingRatio gaps span d * (n + (n-1) * ratio).
        fun span(n: Int) = n + (n - 1) * SpacingRatio
        return minOf(width / span(rows.max()), height / span(rows.size))
    }

    /**
     * Largest diameter (up to [wanted]) for which a grid of at least `2 * count` cells,
     * each one pitch wide, still fits.
     *
     * The dp floor outranks the grid: on a small phone 5 objects cannot have both 126dp
     * targets and 10 cells to scatter across, and shrinking the target is the worse trade
     * for a 3-year-old's motor control. Scatter richness gives way instead, down to the
     * `count` cells the round actually needs.
     */
    private fun scatterFit(count: Int, width: Dp, height: Dp, wanted: Dp, floor: Dp): Dp {
        var d = wanted
        repeat(200) {
            if (cellCount(d, width, height) >= 2 * count) return d
            if (d <= floor && cellCount(d, width, height) >= count) return d
            d *= 0.97f
        }
        return d
    }

    private fun cellCount(diameter: Dp, width: Dp, height: Dp): Int =
        gridFor(diameter, width, height).let { it.first * it.second }

    private fun gridFor(diameter: Dp, width: Dp, height: Dp): Pair<Int, Int> {
        val p = pitch(diameter)
        return floor(width / p).toInt() to floor(height / p).toInt()
    }

    /**
     * Object centres, normalized 0f..1f within the play area.
     *
     * Scattered (1-5): a grid of at least `2 * count` cells, `count` of them chosen at
     * random, jittered up to +-15% of cell size. The jitter is reduced when there is not
     * enough slack to keep the 25% spacing, so objects never crowd or overlap.
     *
     * Structured (6-10): even spacing, no jitter, reads left to right, top to bottom.
     */
    fun positions(
        count: Int,
        width: Dp,
        height: Dp,
        compact: Boolean,
        random: Random = Random.Default,
    ): List<Offset> {
        val d = diameter(count, width, height, compact)
        return if (count <= 5) {
            scattered(count, width, height, d, random)
        } else {
            structured(count, width, height, d, compact)
        }
    }

    private fun structured(
        count: Int,
        width: Dp,
        height: Dp,
        diameter: Dp,
        compact: Boolean,
    ): List<Offset> {
        val rows = rowsThatFit(count, width, height, compact)
        val p = pitch(diameter)
        val blockHeight = p * (rows.size - 1)
        val top = (height - blockHeight) / 2f

        return buildList {
            rows.forEachIndexed { rowIndex, cols ->
                val rowWidth = p * (cols - 1)
                val left = (width - rowWidth) / 2f
                val y = top + p * rowIndex
                repeat(cols) { col ->
                    add(Offset((left + p * col) / width, y / height))
                }
            }
        }
    }

    private fun scattered(
        count: Int,
        width: Dp,
        height: Dp,
        diameter: Dp,
        random: Random,
    ): List<Offset> {
        val (cols, rows) = gridFor(diameter, width, height)
        val cellW = width / cols
        val cellH = height / rows

        // Keep centres at least one pitch apart even at worst-case jitter, and keep the
        // object fully inside its cell.
        fun slack(cell: Dp): Float {
            val fromSpacing = (1f - pitch(diameter) / cell) / 2f
            val fromBounds = (1f - diameter / cell) / 2f
            return minOf(0.15f, fromSpacing, fromBounds).coerceAtLeast(0f)
        }

        val jitterX = slack(cellW)
        val jitterY = slack(cellH)

        val cells = List(cols * rows) { it }.shuffled(random).take(count).sorted()
        return cells.map { index ->
            val col = index % cols
            val row = index / cols
            val cx = cellW * (col + 0.5f) + cellW * jitterX * (random.nextFloat() * 2f - 1f)
            val cy = cellH * (row + 0.5f) + cellH * jitterY * (random.nextFloat() * 2f - 1f)
            Offset(cx / width, cy / height)
        }
    }
}
