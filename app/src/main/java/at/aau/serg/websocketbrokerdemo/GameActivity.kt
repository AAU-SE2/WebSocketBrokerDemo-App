package at.aau.serg.websocketbrokerdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.myapplication.R

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
    }

    private fun isMyTurn(): Boolean {
        val players = ClientState.players
        if (players.isEmpty() || ClientState.currentPlayerIndex >= players.size) return false
        return players[ClientState.currentPlayerIndex].playerId == ClientState.playerId
    }
    private fun setupGameHandlers() {
    }
}