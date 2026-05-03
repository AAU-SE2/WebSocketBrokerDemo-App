package at.aau.serg.websocketbrokerdemo

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import at.aau.serg.websocketbrokerdemo.messaging.dtos.ExistingPlayerDTO
import at.aau.serg.websocketbrokerdemo.model.CardRepository
import at.aau.serg.websocketbrokerdemo.model.ClientState
import at.aau.serg.websocketbrokerdemo.network.lobby.LobbyHandler
import com.example.myapplication.R

class LobbyActivity : ComponentActivity() {

    private val characterIds = listOf("MRS_LAVENDER", "MRS_PINK", "DR_RED", "DR_BLUE")
    private var availableCharacters: List<String> = emptyList()
    private var currentCharacterIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        val btnLeave       = findViewById<Button>(R.id.btnLeave)
        val btnReady       = findViewById<Button>(R.id.btnReady)
        val btnPrev        = findViewById<ImageButton>(R.id.btnPrev)
        val btnNext        = findViewById<ImageButton>(R.id.btnNext)
        val imgMyCharacter = findViewById<ImageView>(R.id.imgMyCharacter)
        val btnStartGame   = findViewById<Button>(R.id.btnStartGame)

        val otherPlayerViews = listOf(
            findViewById<ImageView>(R.id.imgOtherPlayerCharacter2),
            findViewById<ImageView>(R.id.imgOtherPlayerCharacter3),
            findViewById<ImageView>(R.id.imgOtherPlayerCharacter4)
        )

        // ← NEU: beim Start direkt aus ClientState laden
        availableCharacters = ClientState.availableCharacters
        if (availableCharacters.isNotEmpty()) {
            updateMyCharacterImage(imgMyCharacter)
        }
        updateOtherPlayers(ClientState.players, otherPlayerViews)

        btnPrev.setOnClickListener {
            if (availableCharacters.isEmpty()) return@setOnClickListener
            currentCharacterIndex = (currentCharacterIndex - 1 + availableCharacters.size) % availableCharacters.size
            updateMyCharacterImage(imgMyCharacter)
        }

        btnNext.setOnClickListener {
            if (availableCharacters.isEmpty()) return@setOnClickListener
            currentCharacterIndex = (currentCharacterIndex + 1) % availableCharacters.size
            updateMyCharacterImage(imgMyCharacter)
        }

        btnReady.setOnClickListener {
            val selectedCharacter = availableCharacters.getOrNull(currentCharacterIndex) ?: return@setOnClickListener
            ClientState.myCharacter = selectedCharacter
            btnPrev.isEnabled = false
            btnNext.isEnabled = false
            btnReady.isEnabled = false
            // TODO: Ready-Nachricht an Server schicken
        }

        btnStartGame.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        btnLeave.setOnClickListener {
            MyStomp.instance.leaveLobby()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        LobbyHandler.onPlayerRemoved = {
            runOnUiThread {
                finish()
            }
        }

        LobbyHandler.onNewPlayerJoined = { dto ->
            runOnUiThread {
                availableCharacters = dto.availableCharacters
                ClientState.availableCharacters = dto.availableCharacters  // ← speichern
                currentCharacterIndex = 0
                updateMyCharacterImage(imgMyCharacter)
                updateOtherPlayers(dto.existingPlayers, otherPlayerViews)
            }
        }

        LobbyHandler.onPlayerRejoined = { dto ->
            runOnUiThread {
                updateOtherPlayers(dto.existingPlayers, otherPlayerViews)
            }
        }

        LobbyHandler.onGameFull = { dto ->
            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle("Fehler")
                    .setMessage(dto.message)
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
    }

    private fun updateMyCharacterImage(imgView: ImageView) {
        val characterId = availableCharacters.getOrNull(currentCharacterIndex) ?: return
        val card = CardRepository.cards.find { it.cardId == characterId }
        card?.let { imgView.setImageResource(it.imageResId) }
    }

    private fun updateOtherPlayers(players: List<ExistingPlayerDTO>, views: List<ImageView>) {
        // Alle anderen Spieler = alle außer dem eigenen
        val others = players.filter { it.playerId != ClientState.playerId }

        // Alle Views zuerst leeren
        views.forEach { it.setImageDrawable(null) }

        // Jeden anderen Spieler in eine View einsetzen
        others.forEachIndexed { index, player ->
            if (index >= views.size) return
            val card = player.character?.let { char ->
                CardRepository.cards.find { it.cardId == char }
            }
            if (card != null) {
                views[index].setImageResource(card.imageResId)
            } else {
                views[index].setImageResource(android.R.drawable.ic_menu_help) // noch am wählen
            }
        }
    }
}