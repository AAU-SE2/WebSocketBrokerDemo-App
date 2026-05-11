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
    private fun setupGameHandlers() {
    }
}