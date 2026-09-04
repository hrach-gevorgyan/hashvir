package com.hrach.hashvir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hrach.hashvir.audio.SoundBank
import com.hrach.hashvir.game.GameViewModel
import com.hrach.hashvir.game.Layout
import com.hrach.hashvir.game.Stage
import com.hrach.hashvir.ui.CountingScreen
import com.hrach.hashvir.ui.IntroScreen
import com.hrach.hashvir.ui.LearnScreen
import com.hrach.hashvir.ui.MenuScreen
import com.hrach.hashvir.ui.Mode
import com.hrach.hashvir.ui.RecognitionScreen

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Every screen is a light pastel, so the system icons have to be dark or they
        // disappear into the background.
        // Draw behind the system bars so each screen's own colour reaches them.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        setContent {
            // One composable tree for phone and tablet; only the size class differs.
            val compact = calculateWindowSizeClass(this).widthSizeClass ==
                WindowWidthSizeClass.Compact
            App(compact = compact)
        }
    }
}

private sealed interface Screen {
    data object Intro : Screen
    data object Menu : Screen
    data class Playing(val mode: Mode) : Screen
}

@Composable
private fun App(compact: Boolean, game: GameViewModel = viewModel()) {
    val context = LocalContext.current
    val sounds = remember { SoundBank(context) }
    DisposableEffect(sounds) { onDispose { sounds.release() } }

    var screen by remember { mutableStateOf<Screen>(Screen.Intro) }
    val toMenu = {
        sounds.stopAll()
        game.reset()
        screen = Screen.Menu
    }

    BackHandler(enabled = screen != Screen.Menu) { toMenu() }

    // The screens paint edge to edge; each one insets its own content, so the bars pick up
    // the colour of whatever is behind them.
    BoxWithConstraints(Modifier.fillMaxSize()) {
        when (val current = screen) {
            Screen.Intro -> IntroScreen(sounds = sounds, onDone = { screen = Screen.Menu })

            Screen.Menu -> MenuScreen(onPick = { screen = Screen.Playing(it) })

            is Screen.Playing -> {
                game.ensureStage(current.mode, maxWidth, Layout.playHeight(maxHeight), compact)
                val finish = {
                    game.onRoundFinished(current.mode, maxWidth, Layout.playHeight(maxHeight), compact)
                }

                when (val stage = game.stage) {
                    is Stage.Counting -> CountingScreen(
                        round = stage.round,
                        sounds = sounds,
                        compact = compact,
                        onTap = game::onTap,
                        onRoundFinished = finish,
                        onBack = toMenu,
                    )

                    is Stage.Recognition -> RecognitionScreen(
                        round = stage.round,
                        sounds = sounds,
                        onRoundFinished = finish,
                        onBack = toMenu,
                    )

                    is Stage.Learning -> LearnScreen(sounds = sounds, onBack = toMenu)

                    null -> Unit
                }
            }
        }
    }
}
