package at.aau.serg.websocketbrokerdemo

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.ComponentActivity
import at.aau.serg.websocketbrokerdemo.messaging.dtos.ExistingPlayerDTO
import at.aau.serg.websocketbrokerdemo.model.CardRepository
import at.aau.serg.websocketbrokerdemo.model.ClientState
import at.aau.serg.websocketbrokerdemo.network.lobby.LobbyHandler
import com.example.myapplication.R
import java.util.UUID
class LobbyActivity : ComponentActivity() {

    private var availableCharacters: List<String> = emptyList()
    private var currentCharacterIndex = 0
    private var isLeaving = false
    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)
       // ClientState.playerId = UUID.randomUUID().toString()
       // Log.d("DEBUG", "PLAYER_ID = ${ClientState.playerId}")
       // MyStomp.instance.connect()
        val imgMyCharacter = findViewById<ImageView>(R.id.imgMyCharacter)

        val btnPrev = findViewById<ImageButton>(R.id.btnPrev)
        val btnNext = findViewById<ImageButton>(R.id.btnNext)
        val btnReady = findViewById<Button>(R.id.btnReady)
        val btnLeave = findViewById<Button>(R.id.btnLeave)
        val btnStartGame = findViewById<Button>(R.id.btnStartGame)

        val otherPlayerViews = listOf(
            findViewById<ImageView>(R.id.imgOtherPlayerCharacter2),
            findViewById<ImageView>(R.id.imgOtherPlayerCharacter3),
            findViewById<ImageView>(R.id.imgOtherPlayerCharacter4)
        )

        // NEU: beim Start direkt aus ClientState laden
        availableCharacters = ClientState.availableCharacters.toList()

        Log.d("LOBBY", "INIT characters = $availableCharacters")
/*
        currentCharacterIndex = 0
        updateMyCharacterImage(imgMyCharacter)
        updateOtherPlayers(ClientState.players, otherPlayerViews)

 */
        currentCharacterIndex = 0
        if (availableCharacters.isNotEmpty()) {
            ClientState.myCharacter = availableCharacters[0] // ← ADD
        }
        updateMyCharacterImage(imgMyCharacter)
        btnNext.setOnClickListener {
            if (availableCharacters.isEmpty()) return@setOnClickListener

            currentCharacterIndex =
                (currentCharacterIndex + 1) % availableCharacters.size
            ClientState.myCharacter = availableCharacters[currentCharacterIndex] // ← ADD
            updateMyCharacterImage(imgMyCharacter)
        }
        btnPrev.setOnClickListener {
            if (availableCharacters.isEmpty()) return@setOnClickListener

            currentCharacterIndex =
                (currentCharacterIndex - 1 + availableCharacters.size) % availableCharacters.size
            ClientState.myCharacter = availableCharacters[currentCharacterIndex] // ← ADD

            updateMyCharacterImage(imgMyCharacter)
        }
        btnReady.setOnClickListener {
            if (availableCharacters.isEmpty()) return@setOnClickListener
            val selectedCharacter = availableCharacters.getOrNull(currentCharacterIndex) ?: return@setOnClickListener
            ClientState.myCharacter = selectedCharacter
            isReady = true
            lockCharacterSelection()
            MyStomp.instance.setReady(selectedCharacter, true)
        }

        btnStartGame.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        btnLeave.setOnClickListener {
            if (isLeaving) return@setOnClickListener
            isLeaving = true
            MyStomp.instance.leaveLobby()
            finish()
        }
// ---------------------------------lobbyHandler -----------------------------------
        LobbyHandler.onNewPlayerJoined = { dto ->
            runOnUiThread {
                ClientState.players = dto.existingPlayers
                ClientState.availableCharacters = dto.availableCharacters
                availableCharacters = dto.availableCharacters.ifEmpty {
                    ClientState.availableCharacters
                }

                Log.d("LOBBY", "UPDATED characters = $availableCharacters")
/*
                currentCharacterIndex = 0

                updateMyCharacterImage(imgMyCharacter)
                updateOtherPlayers(dto.existingPlayers, otherPlayerViews)


 */
                if (availableCharacters.isNotEmpty()) {
                ClientState.myCharacter = availableCharacters[0]
            }

                updateMyCharacterImage(imgMyCharacter)
                updateOtherPlayers(dto.existingPlayers, otherPlayerViews)
            }


        }

        LobbyHandler.onSetReady = { dto ->
            runOnUiThread {

                ClientState.players = dto.existingPlayers
                ClientState.availableCharacters = dto.availableCharacters

                availableCharacters = dto.availableCharacters.ifEmpty {
                    ClientState.availableCharacters
                }

                updateMyCharacterImage(imgMyCharacter)
                updateOtherPlayers(dto.existingPlayers, otherPlayerViews)
            }
        }

        LobbyHandler.onPlayerRemoved = {
            runOnUiThread { finish() }
        }

        LobbyHandler.onOtherPlayerRemoved = { playerId ->
            runOnUiThread {
                val updated = ClientState.players.filter { it.playerId != playerId }
                ClientState.players = updated
                updateOtherPlayers(updated, otherPlayerViews)
            }
        }

        LobbyHandler.onGameFull = { dto ->
            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle("Fehler")
                    .setMessage(dto.message)
                    .setPositiveButton("OK") { d, _ -> d.dismiss() }
                    .show()
            }
        }
    }
    private fun lockCharacterSelection() {
        findViewById<ImageButton>(R.id.btnPrev).isEnabled = false
        findViewById<ImageButton>(R.id.btnNext).isEnabled = false
        findViewById<Button>(R.id.btnReady).isEnabled = false
    }
    private fun updateMyCharacterImage(imgView: ImageView) {

        val characterId = ClientState.myCharacter

        Log.d("LOBBY", "render character = $characterId")

        if (characterId == null) {
            imgView.setImageResource(android.R.drawable.ic_menu_help)
            return
        }

        val card = CardRepository.cards.find { it.cardId == characterId }

        imgView.setImageResource(
            card?.imageResId ?: android.R.drawable.ic_menu_help
        )
    }

    private fun updateOtherPlayers(
        players: List<ExistingPlayerDTO>,
        views: List<ImageView>
    ) {
        val others = players.filter { it.playerId != ClientState.playerId }

        // Alle Views zuerst leeren
        views.forEach { it.setImageDrawable(null) }

        // Jeden anderen Spieler in eine View einsetzen
        others.forEachIndexed { index, player ->
            if (index >= views.size) return

            val card = player.character?.let {
                CardRepository.cards.find { c -> c.cardId == it }
            }

            views[index].setImageResource(
                card?.imageResId ?: android.R.drawable.ic_menu_help
            )
        }

    }
    /*
    private fun getMyCharacter(): String? {
        return ClientState.players
            .find { it.playerId == ClientState.playerId }
            ?.character
    }

     */
}