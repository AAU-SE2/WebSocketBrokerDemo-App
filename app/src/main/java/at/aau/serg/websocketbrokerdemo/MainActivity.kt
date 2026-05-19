package at.aau.serg.websocketbrokerdemo

import MyStomp
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.R
import android.widget.Button;
import android.content.Intent
import android.util.Log
import at.aau.serg.websocketbrokerdemo.model.ClientState
import at.aau.serg.websocketbrokerdemo.network.lobby.LobbyHandler
class MainActivity : ComponentActivity(), Callbacks {
    private lateinit var myStomp: MyStomp



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Always create a fresh MyStomp — it resets internal state cleanly
        // and keeps the CoroutineScope alive for reconnection.
        myStomp = MyStomp(this)

        val playerId = UserPreferences.getOrCreatePlayerId(this)
        ClientState.playerId = playerId

        enableEdgeToEdge()

        setContentView(R.layout.cluedo_fragment_fullscreen)

        val loadingOverlay = findViewById<android.widget.FrameLayout>(R.id.loadingOverlay)

        LobbyHandler.onLobbyJoined = {
            runOnUiThread {
                loadingOverlay.visibility = android.view.View.GONE
                startActivity(Intent(this, LobbyActivity::class.java))
            }
        }

        LobbyHandler.onPlayerRejoined = { dto ->
            runOnUiThread {
                if (ClientState.gameStatus == "RUNNING") {
                    startActivity(Intent(this, GameActivity::class.java))
                } else {
                    startActivity(Intent(this, LobbyActivity::class.java))
                }
            }
        }
        LobbyHandler.onPlayerRejoinedRunning = {
            runOnUiThread {
                loadingOverlay.visibility = android.view.View.GONE
                startActivity(Intent(this, GameActivity::class.java))
            }
        }

        val btnLearn = findViewById<Button>(R.id.btnLearn)

        btnLearn.setOnClickListener {
            val intent = Intent(this, LearnActivity::class.java)
            startActivity(intent)
        }


        val btnStart = findViewById<Button>(R.id.btnStart)
        btnStart.setOnClickListener {
            loadingOverlay.visibility = android.view.View.VISIBLE
            myStomp.connect()
        }

    }

    override fun onDestroy() {
        LobbyHandler.onLobbyJoined = null
        LobbyHandler.onPlayerRejoined = null
        LobbyHandler.onPlayerRejoinedRunning = null
        if (::myStomp.isInitialized) {
            myStomp.disconnect()
        }
        super.onDestroy()
    }

    override fun onResponse(res: String) {
        Log.d("MainActivity", "Response: $res")
    }

    override fun onConnected() {
        // Handled via LobbyHandler.onLobbyJoined callback
    }
}