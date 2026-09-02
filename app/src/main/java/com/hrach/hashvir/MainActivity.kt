package com.hrach.hashvir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.game.GameViewModel
import com.hrach.hashvir.game.Stage
import com.hrach.hashvir.ui.CountingScreen
import com.hrach.hashvir.ui.RecognitionScreen

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // One composable tree for phone and tablet; only the size class differs.
            val compact = calculateWindowSizeClass(this).widthSizeClass ==
                WindowWidthSizeClass.Compact
            App(compact = compact)
        }
    }
}

@Composable
private fun App(compact: Boolean, game: GameViewModel = viewModel()) {
    val context = LocalContext.current
    val sounds = remember { SoundBank(context) }
    DisposableEffect(sounds) { onDispose { sounds.release() } }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        game.ensureStage(maxWidth, maxHeight, compact)

        val finish = { game.onRoundFinished(maxWidth, maxHeight, compact) }
        when (val stage = game.stage) {
            is Stage.Counting -> CountingScreen(
                round = stage.round,
                sounds = sounds,
                compact = compact,
                onTap = game::onTap,
                onRoundFinished = finish,
            )

            is Stage.Recognition -> RecognitionScreen(
                round = stage.round,
                sounds = sounds,
                onRoundFinished = finish,
            )

            null -> Unit
        }
    }
}
