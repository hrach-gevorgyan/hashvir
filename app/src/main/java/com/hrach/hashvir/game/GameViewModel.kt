package com.hrach.hashvir.game

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.AndroidViewModel
import com.hrach.hashvir.theme.BackgroundTint
import com.hrach.hashvir.ui.Mode
import kotlin.random.Random

private const val PREFS = "hashvir"
private const val KEY_ROUNDS_COMPLETED = "roundsCompleted"

sealed interface Stage {
    data class Counting(val round: Round) : Stage
    data class Recognition(val round: RecognitionRound) : Stage

    /** Սովորել keeps its own state inside the screen; the stage just selects it. */
    data object Learning : Stage
}

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val random = Random.Default

    /** Persisted, and never shown to the child. Kept for the parent screen later. */
    var roundsCompleted: Int = prefs.getInt(KEY_ROUNDS_COMPLETED, 0)
        private set

    var stage by mutableStateOf<Stage?>(null)
        private set

    private val countingRound: Round? get() = (stage as? Stage.Counting)?.round

    /** Builds the first stage of a mode, and rebuilds on rotation or a size change. */
    fun ensureStage(mode: Mode, width: Dp, height: Dp, compact: Boolean) {
        if (stage == null) stage = generate(mode, width, height, compact)
    }

    /** Leaving a mode clears it, so the next one starts fresh. */
    fun reset() {
        stage = null
    }

    fun onTap(index: Int) {
        val current = countingRound ?: return
        if (index in current.tapped) return
        stage = Stage.Counting(current.copy(tapped = current.tapped + index))
    }

    fun onRoundFinished(mode: Mode, width: Dp, height: Dp, compact: Boolean) {
        roundsCompleted += 1
        prefs.edit().putInt(KEY_ROUNDS_COMPLETED, roundsCompleted).apply()
        stage = generate(mode, width, height, compact)
    }

    private fun generate(mode: Mode, width: Dp, height: Dp, compact: Boolean): Stage {
        val previousBackground = when (val current = stage) {
            is Stage.Counting -> current.round.background
            is Stage.Recognition -> current.round.background
            else -> null
        }
        val background = nextBackground(previousBackground)

        return when (mode) {
            Mode.Learn -> Stage.Learning

            Mode.Guess -> {
                val answer = nextCount(
                    (stage as? Stage.Recognition)?.round?.answer,
                    random = random,
                )
                Stage.Recognition(
                    RecognitionRound(
                        answer = answer,
                        choices = choicesFor(answer, random),
                        // Cards are paper; the ground must not be, or they vanish into it.
                        background = if (background == BackgroundTint.Paper) {
                            nextBackground(background)
                        } else {
                            background
                        },
                    )
                )
            }

            Mode.Count -> {
                val previous = countingRound
                val count = nextCount(previous?.count, random = random)
                Stage.Counting(
                    Round(
                        count = count,
                        fruit = nextFruit(previous?.fruit, random),
                        background = background,
                        positions = Layout.positions(count, width, height, compact, random),
                    )
                )
            }
        }
    }
}
