package com.hrach.hashvir.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.util.Log
import com.hrach.hashvir.BuildConfig
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.game.Layout
import com.hrach.hashvir.game.Round
import kotlinx.coroutines.delay

/** Beat between the last tap and the numeral, so the count lands before the answer does. */
private const val GLYPH_DELAY_MS = 400L
private const val CHIME_DELAY_MS = 600L

/** Long enough to look at the numeral, short enough that she does not go looking elsewhere. */
private const val NEXT_ROUND_DELAY_MS = 1500L

@Composable
fun CountingScreen(
    round: Round,
    sounds: SoundBank,
    compact: Boolean,
    onTap: (Int) -> Unit,
    onRoundFinished: () -> Unit,
    modifier: Modifier = Modifier,
    debugOverlay: Boolean = false,
) {
    // Keyed on the round's identity, not the Round instance: tapping copies the round, and
    // remember(round) would reset the glyph on every tap. Neither count nor object type
    // repeats consecutively, so this key always changes between rounds.
    var showGlyph by remember(round.count, round.objectType) { mutableStateOf(false) }

    LaunchedEffect(round.isComplete) {
        if (!round.isComplete) return@LaunchedEffect
        delay(GLYPH_DELAY_MS)
        showGlyph = true
        sounds.play("total_${round.count}")
        delay(CHIME_DELAY_MS)
        sounds.play("chime")
        delay(NEXT_ROUND_DELAY_MS)
        onRoundFinished()
    }

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(round.background.color)
    ) {
        val shorter = minOf(maxWidth, maxHeight)
        val diameter = Layout.diameter(round.count, maxWidth, maxHeight, compact)

        if (BuildConfig.DEBUG && diameter < Layout.PhoneFloor) {
            Log.w(
                "Layout",
                "count=${round.count} target ${diameter.value.toInt()}dp is below the " +
                    "${Layout.PhoneFloor.value.toInt()}dp floor in ${maxWidth.value.toInt()}x" +
                    "${maxHeight.value.toInt()}dp",
            )
        }

        for ((index, position) in round.positions.withIndex()) {
            ObjectSprite(
                type = round.objectType,
                diameter = diameter,
                tapped = index in round.tapped,
                onTap = {
                    if (index !in round.tapped) {
                        // The number she hears is the one she is on, not a running total.
                        sounds.play("num_${round.tapped.size + 1}")
                        onTap(index)
                    }
                },
                modifier = Modifier.offset(
                    x = maxWidth * position.x - diameter / 2,
                    y = maxHeight * position.y - diameter / 2,
                ),
            )
        }

        if (debugOverlay) {
            TargetBoundsOverlay(round, diameter, maxWidth, maxHeight)
        }

        if (showGlyph) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(shorter * 0.04f),
                ) {
                    Stars(sounds = sounds, starSize = shorter * 0.13f)
                    NumberGlyph(count = round.count, glyphHeight = shorter * 0.40f)
                }
            }
        }

        // Still and minimised while objects are tappable; she only comes up at the boundary.
        PouyPouy(
            state = if (showGlyph) HelperState.Happy else HelperState.Still,
            size = maxHeight * if (showGlyph) 0.15f else 0.08f,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(shorter * 0.03f),
        )
    }
}
