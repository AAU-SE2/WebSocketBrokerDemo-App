package at.aau.serg.websocketbrokerdemo

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import android.view.View
import androidx.activity.ComponentActivity
import at.aau.serg.websocketbrokerdemo.messaging.dtos.ExistingPlayerDTO
import at.aau.serg.websocketbrokerdemo.model.CardRepository
import at.aau.serg.websocketbrokerdemo.model.ClientState
import at.aau.serg.websocketbrokerdemo.network.lobby.LobbyHandler
import com.example.myapplication.R
import java.util.UUID
import at.aau.serg.websocketbrokerdemo.GameActivity
class LobbyActivity : ComponentActivity() {

    private var availableCharacters: List<String> = emptyList()
    private var currentCharacterIndex = 0
    private var isLeaving = false
    private var isReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        val loadingOverlay = findViewById<android.widget.FrameLayout>(R.id.loadingOverlay)
        loadingOverlay.visibility = android.view.View.VISIBLE

        if (ClientState.players.isNotEmpty()) {
            loadingOverlay.visibility = android.view.View.GONE
        }

        LobbyHandler.onGameStarted = {
            runOnUiThread {
                val intent = Intent(this, GameActivity::class.java)
                startActivity(intent)
            }
        }
        LobbyHandler.onStartGameError = { reason ->
            runOnUiThread {
                Toast.makeText(this, reason, Toast.LENGTH_SHORT).show()
            }
        }
        LobbyHandler.onError = { reason ->
            runOnUiThread {
                Toast.makeText(this, reason, Toast.LENGTH_SHORT).show()
            }
        }
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

        val otherReadyChecks = listOf(
            findViewById<ImageView>(R.id.imgReadyCheck2),
            findViewById<ImageView>(R.id.imgReadyCheck3),
            findViewById<ImageView>(R.id.imgReadyCheck4)
        )

        availableCharacters = ClientState.availableCharacters.toList()
        isReady = false
        btnPrev.visibility = View.VISIBLE
        btnNext.visibility = View.VISIBLE
        btnReady.isEnabled = true
        findViewById<ImageView>(R.id.imgReadyCheck).visibility = View.GONE

        Log.d("LOBBY", "INIT characters = $availableCharacters")

        val me = ClientState.players.find { it.playerId == ClientState.playerId }
        if (me != null && me.character != null) {
            ClientState.myCharacter = me.character
            if (me.ready) {
                isReady = true
                lockCharacterSelection()
            }
        } else {
            currentCharacterIndex = 0
            if (availableCharacters.isNotEmpty()) {
                ClientState.myCharacter = availableCharacters[0]
            }
        }
        updateMyCharacterImage(imgMyCharacter)
        updateOtherPlayers(ClientState.players, otherPlayerViews, otherReadyChecks)

        btnNext.setOnClickListener {
            if (availableCharacters.isEmpty()) return@setOnClickListener

            currentCharacterIndex =
                (currentCharacterIndex + 1) % availableCharacters.size
            ClientState.myCharacter = availableCharacters[currentCharacterIndex]
            updateMyCharacterImage(imgMyCharacter)
        }
        btnPrev.setOnClickListener {
            if (availableCharacters.isEmpty()) return@setOnClickListener

            currentCharacterIndex =
                (currentCharacterIndex - 1 + availableCharacters.size) % availableCharacters.size
            ClientState.myCharacter = availableCharacters[currentCharacterIndex]

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
            MyStomp.instance.startGame()
        }

        btnLeave.setOnClickListener {
            if (isLeaving) return@setOnClickListener
            isLeaving = true
            btnLeave.isEnabled = false
            MyStomp.instance.leaveLobby()
            MyStomp.instance.disconnect()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        LobbyHandler.onNewPlayerJoined = { dto ->
            runOnUiThread {
                loadingOverlay.visibility = android.view.View.GONE
                ClientState.players = dto.existingPlayers
                ClientState.availableCharacters = dto.availableCharacters
                availableCharacters = dto.availableCharacters.ifEmpty {
                    ClientState.availableCharacters
                }

                Log.d("LOBBY", "UPDATED characters = $availableCharacters")

                if (availableCharacters.isNotEmpty() && !isReady) {
                    ClientState.myCharacter = availableCharacters[0]
                }

                updateMyCharacterImage(imgMyCharacter)
                updateOtherPlayers(dto.existingPlayers, otherPlayerViews, otherReadyChecks)
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
                updateOtherPlayers(dto.existingPlayers, otherPlayerViews, otherReadyChecks)
            }
        }

        LobbyHandler.onPlayerRemoved = {
            runOnUiThread {
                MyStomp.instance.disconnect()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }

        LobbyHandler.onOtherPlayerRemoved = { playerId ->
            runOnUiThread {
                val updated = ClientState.players.filter { it.playerId != playerId }
                ClientState.players = updated
                updateOtherPlayers(updated, otherPlayerViews, otherReadyChecks)
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
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        super.onBackPressed()
        if (isLeaving) return
        isLeaving = true
        MyStomp.instance.leaveLobby()
        MyStomp.instance.disconnect()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        LobbyHandler.onLobbyJoined = null
        LobbyHandler.onNewPlayerJoined = null
        LobbyHandler.onPlayerRejoined = null
        LobbyHandler.onPlayerRejoinedRunning = null
        LobbyHandler.onGameFull = null
        LobbyHandler.onPlayerRemoved = null
        LobbyHandler.onOtherPlayerRemoved = null
        LobbyHandler.onSetReady = null
        LobbyHandler.onGameStarted = null
        LobbyHandler.onStartGameError = null
        LobbyHandler.onError = null
        super.onDestroy()
    }

    private fun lockCharacterSelection() {
        findViewById<ImageButton>(R.id.btnPrev).visibility = View.GONE
        findViewById<ImageButton>(R.id.btnNext).visibility = View.GONE
        findViewById<Button>(R.id.btnReady).isEnabled = false
        findViewById<ImageView>(R.id.imgReadyCheck).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnStartGame).alpha = 1f
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
        views: List<ImageView>,
        readyChecks: List<ImageView>
    ) {
        val others = players.filter { it.playerId != ClientState.playerId }

        views.forEach { it.setImageDrawable(null) }
        readyChecks.forEach { it.visibility = View.GONE }

        others.forEachIndexed { index, player ->
            if (index >= views.size) return@forEachIndexed

            val card = player.character?.let {
                CardRepository.cards.find { c -> c.cardId == it }
            }

            views[index].setImageResource(
                card?.imageResId ?: android.R.drawable.ic_menu_help
            )
            readyChecks[index].visibility = if (player.ready) View.VISIBLE else View.GONE
        }
    }
}