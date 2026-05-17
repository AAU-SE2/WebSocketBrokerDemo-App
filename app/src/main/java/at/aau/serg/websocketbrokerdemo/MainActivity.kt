package at.aau.serg.websocketbrokerdemo

import MyStomp
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.R
import android.widget.Button;
import android.content.Intent
import android.util.Log
import at.aau.serg.websocketbrokerdemo.model.ClientState
import at.aau.serg.websocketbrokerdemo.network.lobby.LobbyHandler
class MainActivity : ComponentActivity(), Callbacks {
    lateinit var myStomp: MyStomp
    lateinit var response: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // myStomp = MyStomp(this) // hier wird es bei jedem oncreat neu erstellet so
        if (!::myStomp.isInitialized) {  //  nur einmal erstellen
            myStomp = MyStomp(this)
        }
       // super.onCreate(savedInstanceState)
        // ID wird hier einmalig erstellt und gespeichert
        val playerId = UserPreferences.getOrCreatePlayerId(this)
        ClientState.playerId = playerId

        enableEdgeToEdge()

        LobbyHandler.onLobbyJoined = {
            runOnUiThread {
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
        setContentView(R.layout.cluedo_fragment_fullscreen)

        val btnLearn = findViewById<Button>(R.id.btnLearn)

        btnLearn.setOnClickListener {
            val intent = Intent(this, LearnActivity::class.java)
            startActivity(intent)
        }


        val btnStart = findViewById<Button>(R.id.btnStart)
        btnStart.setOnClickListener {
            myStomp.connect()
            //TEST: ACHTUNG NUR ZUM TESTEN!!!!
            //startActivity(Intent(this, LobbyActivity::class.java))
        }

        
        /*findViewById<Button>(R.id.connectbtn).setOnClickListener { myStomp.connect() }
        findViewById<Button>(R.id.hellobtn).setOnClickListener { myStomp.sendHello() }
        findViewById<Button>(R.id.jsonbtn).setOnClickListener { myStomp.sendJson() }
        findViewById<Button>(R.id.joinlobbybtn).setOnClickListener {
            myStomp.joinLobby("Test", "Test")
        }
        response = findViewById(R.id.response_view)
    */}

    override fun onResponse(res: String) {
        // response.text = res
        Log.d("MainActivity", "Response: $res")
    }

    override fun onConnected() {
        val intent = Intent(this, LobbyActivity::class.java)
        startActivity(intent)
    }

    fun onJoinSuccess(message: String) {
        response.text = message
    }

    fun onPlayersReceived(players: List<String>) {
        response.text = players.joinToString(", ")
    }
}