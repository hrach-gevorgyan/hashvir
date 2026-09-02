package com.hrach.hashvir.game

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.AndroidViewModel
import com.hrach.hashvir.theme.BackgroundTint
import kotlin.random.Random

private const val PREFS = "hashvir"
private const val KEY_ROUNDS_COMPLETED = "roundsCompleted"

/** Recognition mode stays out of the way until counting is familiar. */
private const val RECOGNITION_UNLOCK = 30

/** Then three of one, three of the other. */
private const val PHASE_LENGTH = 3

sealed interface Stage {
    data class Counting(val round: Round) : Stage
    data class Recognition(val round: RecognitionRound) : Stage
}

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val random = Random.Default

    /** Persisted. Used only to unlock recognition mode; never shown to the child. */
    var roundsCompleted: Int = prefs.getInt(KEY_ROUNDS_COMPLETED, 0)
        private set

    var stage by mutableStateOf<Stage?>(null)
        private set

    private val countingRound: Round? get() = (stage as? Stage.Counting)?.round

    /** Builds the first stage, and rebuilds on rotation or a size change. */
    fun ensureStage(width: Dp, height: Dp, compact: Boolean) {
        if (stage == null) stage = generate(width, height, compact)
    }

    fun onTap(index: Int) {
        val current = countingRound ?: return
        if (index in current.tapped) return
        stage = Stage.Counting(current.copy(tapped = current.tapped + index))
    }

    /** Called once the round is over: after the chime, or after the praise. */
    fun onRoundFinished(width: Dp, height: Dp, compact: Boolean) {
        roundsCompleted += 1
        prefs.edit().putInt(KEY_ROUNDS_COMPLETED, roundsCompleted).apply()
        stage = generate(width, height, compact)
    }

    /**
     * Counting until [RECOGNITION_UNLOCK] rounds are behind her, then three of each in turn.
     * Derived from the persisted count rather than held separately, so it survives a restart.
     */
    private fun isRecognitionTurn(): Boolean {
        if (roundsCompleted < RECOGNITION_UNLOCK) return false
        val since = roundsCompleted - RECOGNITION_UNLOCK
        return (since / PHASE_LENGTH) % 2 == 1
    }

    private fun generate(width: Dp, height: Dp, compact: Boolean): Stage {
        val previousBackground = when (val current = stage) {
            is Stage.Counting -> current.round.background
            is Stage.Recognition -> current.round.background
            null -> null
        }
        val background = nextBackground(previousBackground)

        if (isRecognitionTurn()) {
            val answer = nextCount(
                (stage as? Stage.Recognition)?.round?.answer,
                random = random,
            )
            return Stage.Recognition(
                RecognitionRound(
                    answer = answer,
                    choices = choicesFor(answer, random),
                    // Cards are paper; the ground must not be, or they disappear into it.
                    background = if (background == BackgroundTint.Paper) {
                        nextBackground(background)
                    } else {
                        background
                    },
                )
            )
        }

        val previous = countingRound
        val count = nextCount(previous?.count, random = random)
        return Stage.Counting(
            Round(
                count = count,
                objectType = nextObjectType(previous?.objectType, random),
                background = background,
                positions = Layout.positions(count, width, height, compact, random),
            )
        )
    }
}
