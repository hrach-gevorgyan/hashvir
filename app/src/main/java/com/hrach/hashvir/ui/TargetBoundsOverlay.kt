package com.hrach.hashvir.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.hrach.hashvir.game.Layout
import com.hrach.hashvir.game.Round

/**
 * Debug only, drawn when `debugOverlay` is set. Boxes the real tap target of every object and
 * prints its size in true millimetres off the display's own xdpi, so the 20mm guidance can be
 * checked against the physical device rather than against dp.
 */
@Composable
fun TargetBoundsOverlay(round: Round, diameter: Dp, width: Dp, height: Dp) {
    val metrics = LocalContext.current.resources.displayMetrics
    val mm = diameter.value * metrics.density / metrics.xdpi * 25.4f
    val ok = diameter >= Layout.Floor
    val marker = if (ok) Color(0xFF3BA55C) else Color(0xFFC9A227)

    Canvas(Modifier.fillMaxSize()) {
        for (position in round.positions) {
            val d = diameter.toPx()
            drawRect(
                color = marker,
                topLeft = Offset(
                    width.toPx() * position.x - d / 2f,
                    height.toPx() * position.y - d / 2f,
                ),
                size = Size(d, d),
                style = Stroke(width = 1.dp.toPx()),
            )
        }
    }

    Box(Modifier.fillMaxSize().padding(8.dp)) {
        Text(
            text = "n=${round.count}  ${diameter.value.toInt()}dp  ${"%.1f".format(mm)}mm" +
                "  ${width.value.toInt()}x${height.value.toInt()}dp" +
                if (ok) "" else "  UNDER 126dp",
            color = marker,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
        )
    }
}
