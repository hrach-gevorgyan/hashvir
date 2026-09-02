package com.hrach.hashvir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.game.Layout
import com.hrach.hashvir.game.Round
import com.hrach.hashvir.theme.BackgroundTint
import com.hrach.hashvir.theme.ObjectType
import com.hrach.hashvir.ui.CountingScreen

/** Debug-only. Tap anywhere outside an object to step through counts 1..10 with the overlay on. */
class LayoutDebugActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val compact = calculateWindowSizeClass(this).widthSizeClass ==
                WindowWidthSizeClass.Compact
            val context = LocalContext.current
            val sounds = remember { SoundBank(context) }
            var count by remember { mutableIntStateOf(1) }

            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .clickable { count = if (count == 10) 1 else count + 1 }
            ) {
                val round = Round(
                    count = count,
                    objectType = ObjectType.entries[count % ObjectType.entries.size],
                    background = BackgroundTint.entries[count % BackgroundTint.entries.size],
                    positions = Layout.positions(count, maxWidth, maxHeight, compact),
                )
                CountingScreen(
                    round = round,
                    sounds = sounds,
                    compact = compact,
                    onTap = {},
                    onRoundFinished = {},
                    debugOverlay = true,
                )
            }
        }
    }
}
