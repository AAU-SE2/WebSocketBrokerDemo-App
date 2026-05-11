package at.aau.serg.websocketbrokerdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.myapplication.R

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
    }

    private fun setupBoard() {
        val container = findViewById<View>(R.id.boardContainer)
        val imgH = container.height
        val imgY = container.top

        if (imgH == 0) return

        val gridW = gridOverlay.width
        val gridH = gridOverlay.height

        val cellW = gridW / BoardConfig.COLS
        val cellH = gridH / BoardConfig.ROWS

        // Clear any previous children (safety for config changes)
        gridOverlay.removeAllViews()

        // Create tappable grid cells
        for (row in 0 until BoardConfig.ROWS) {
            for (col in 0 until BoardConfig.COLS) {
                val cell = View(this)
                val clp = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(cellW, cellH).apply {
                    startToStart = androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
                    topToTop = androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
                    leftMargin = col * cellW
                    topMargin = row * cellH
                }
                cell.layoutParams = clp
                cell.setBackgroundColor(Color.TRANSPARENT)
                cell.setOnClickListener { onCellTapped(col, row) }
                gridOverlay.addView(cell)
            }
        }

    private fun isMyTurn(): Boolean {
        val players = ClientState.players
        if (players.isEmpty() || ClientState.currentPlayerIndex >= players.size) return false
        return players[ClientState.currentPlayerIndex].playerId == ClientState.playerId
    }
        private fun onSuggest() {
            if (!isMyTurn() || ClientState.isEliminated) return
            val pos = ClientState.playerPositions[ClientState.playerId] ?: ""
            if (ClientState.currentPhase != "IN_ROOM" && ClientState.currentPhase != "WAITING_FOR_ROLL") return
            if (!BoardConfig.ROOM_CENTERS_PERCENT.containsKey(pos)) return

            dialogOverlay.visibility = View.VISIBLE
            GameUIHelper.showCardSelectionOverlay(
                this, dialogOverlay, "SUGGESTION",
                includeRooms = false,
                currentRoom = pos
            ) { suspect, room, weapon ->
                dialogOverlay.visibility = View.GONE
                MyStomp.instance.makeSuggestion(suspect, room, weapon)
            }
        }

        private fun onAccuse() {
            if (!isMyTurn() || ClientState.isEliminated) return
            val pos = ClientState.playerPositions[ClientState.playerId] ?: ""
            if (ClientState.currentPhase != "IN_ROOM" && ClientState.currentPhase != "WAITING_FOR_ROLL") return
            if (!BoardConfig.ROOM_CENTERS_PERCENT.containsKey(pos)) return

            dialogOverlay.visibility = View.VISIBLE
            GameUIHelper.showCardSelectionOverlay(
                this, dialogOverlay, "ACCUSATION",
                includeRooms = true,
                currentRoom = null
            ) { suspect, room, weapon ->
                dialogOverlay.visibility = View.GONE
                MyStomp.instance.makeAccusation(suspect, room, weapon)
            }
        }

    private fun setupGameHandlers() {
        GameHandler.onSuggestionResult = { suggesterID, suspect, room, weapon, matchingCards ->
            runOnUiThread {
                GameUIHelper.showSuggestionTimer(this, rootLayout) {
                    // Only show matching cards to the SUGGESTER
                    if (suggesterID == ClientState.playerId) {
                        if (matchingCards.isNotEmpty()) {
                            GameUIHelper.showResultCards(this, rootLayout, matchingCards)
                        } else {
                            Toast.makeText(this, getString(R.string.no_matching_cards), Toast.LENGTH_SHORT).show()
                        }
                        updateChecklist()
                    } else {
                        Toast.makeText(this, getString(R.string.suggestion_made, "${suggesterID.take(8)}..."), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        GameHandler.onAccusation = { accuserID, suspect, room, weapon, correct, eliminated ->
            runOnUiThread {
                // All players see the accusation cards
                GameUIHelper.showResultCards(this, rootLayout, listOf(suspect, weapon, room), 3000)
                if (correct) {
                    val msg = if (accuserID == ClientState.playerId) getString(R.string.you_won) else getString(R.string.player_won, "${accuserID.take(8)}...")
                    android.os.Handler(mainLooper).postDelayed({
                        GameUIHelper.showGameEndOverlay(this, rootLayout, msg)
                    }, 3500)
                } else if (eliminated) {
                    // Only show elimination message, differentiate by playerId
                    if (accuserID == ClientState.playerId) {
                        Toast.makeText(this, getString(R.string.wrong_accusation), Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, getString(R.string.player_eliminated, "${accuserID.take(8)}..."), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    }
}