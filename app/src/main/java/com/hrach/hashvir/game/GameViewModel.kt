package com.hrach.hashvir.game

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.AndroidViewModel
import kotlin.random.Random

private const val PREFS = "hashvir"
private const val KEY_ROUNDS_COMPLETED = "roundsCompleted"

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val random = Random.Default

    /** Persisted. Used only to unlock recognition mode; never shown to the child. */
    var roundsCompleted: Int = prefs.getInt(KEY_ROUNDS_COMPLETED, 0)
        private set

    var round by mutableStateOf<Round?>(null)
        private set

    /** Builds the first round, and rebuilds on rotation or a size change. */
    fun ensureRound(width: Dp, height: Dp, compact: Boolean) {
        if (round == null) round = generate(width, height, compact)
    }

    fun onTap(index: Int) {
        val current = round ?: return
        if (index in current.tapped) return
        round = current.copy(tapped = current.tapped + index)
    }

    /** Called once the chime has played and the round is over. */
    fun onRoundFinished(width: Dp, height: Dp, compact: Boolean) {
        roundsCompleted += 1
        prefs.edit().putInt(KEY_ROUNDS_COMPLETED, roundsCompleted).apply()
        round = generate(width, height, compact)
    }

    private fun generate(width: Dp, height: Dp, compact: Boolean): Round {
        val previous = round
        val count = nextCount(previous?.count, random = random)
        return Round(
            count = count,
            objectType = nextObjectType(previous?.objectType, random),
            background = nextBackground(previous?.background),
            positions = Layout.positions(count, width, height, compact, random),
        )
    }
}
