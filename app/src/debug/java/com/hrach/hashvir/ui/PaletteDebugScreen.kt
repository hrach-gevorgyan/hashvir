package com.hrach.hashvir.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.theme.Armenian
import com.hrach.hashvir.theme.BackgroundTint
import com.hrach.hashvir.theme.Ink
import com.hrach.hashvir.theme.ObjectType
import com.hrach.hashvir.theme.contrastOn
import com.hrach.hashvir.theme.contrastRatio
import com.hrach.hashvir.theme.edgeColor

/**
 * Debug-only. Every object colour on every background with the computed contrast ratio,
 * plus a font specimen covering the number words and the numerals.
 */
@Composable
fun PaletteDebugScreen() {
    LazyColumn(Modifier.fillMaxWidth()) {
        item { AudioProbe() }
        item { FontSpecimen() }
        items(ObjectType.entries) { obj -> ObjectRow(obj) }
        item { InkRow() }
    }
}

/** Step 3 check: does a tap-to-sound feel instant, and does a missing clip stay silent? */
@Composable
private fun AudioProbe() {
    val context = LocalContext.current
    val bank = remember { SoundBank(context) }
    DisposableEffect(bank) { onDispose { bank.release() } }
    Row(
        Modifier
            .fillMaxWidth()
            .background(BackgroundTint.Sage.color)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(onClick = { bank.play("num_1") }) { Text("num_1") }
        Button(onClick = { bank.play("chime") }) { Text("chime") }
    }
}

@Composable
private fun ObjectRow(obj: ObjectType) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        for (tint in BackgroundTint.entries) {
            val ratio = obj.contrastOn(tint)
            Column(
                Modifier
                    .weight(1f)
                    .background(tint.color)
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Swatch(obj)
                Text(
                    text = "%.2f".format(ratio),
                    color = Ink.Primary,
                    fontWeight = if (ratio < 4.5) FontWeight.Black else FontWeight.Normal,
                    fontSize = 12.sp,
                )
                Text(if (ratio >= 4.5) "ok" else "FAIL", color = Ink.Primary, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun Swatch(obj: ObjectType) {
    Canvas(Modifier.size(44.dp)) {
        val r = size.minDimension / 2f
        drawCircle(obj.color, radius = r)
        drawCircle(
            obj.edgeColor,
            radius = r - ObjectType.OutlineWidthDp.dp.toPx() / 2f,
            style = Stroke(width = ObjectType.OutlineWidthDp.dp.toPx()),
        )
    }
}

@Composable
private fun InkRow() {
    Row(Modifier.fillMaxWidth()) {
        for (tint in BackgroundTint.entries) {
            Box(
                Modifier
                    .weight(1f)
                    .background(tint.color)
                    .padding(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "7  %.1f".format(contrastRatio(Ink.Primary, tint.color)),
                    color = Ink.Primary,
                    fontFamily = Armenian,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun FontSpecimen() {
    Column(
        Modifier
            .fillMaxWidth()
            .background(BackgroundTint.Paper.color)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            "1234567890",
            fontFamily = Armenian,
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            color = Ink.Primary,
        )
        Text(
            "\u0578\u0582  \u0587  \u0568  \u0540\u0561\u0577\u057E\u056B\u0572",
            fontFamily = Armenian,
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            color = Ink.Primary,
        )
    }
}
