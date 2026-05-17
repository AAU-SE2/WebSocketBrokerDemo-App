package at.aau.serg.websocketbrokerdemo

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import android.content.Intent
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.myapplication.R
import at.aau.serg.websocketbrokerdemo.model.BoardConfig
import at.aau.serg.websocketbrokerdemo.model.ClientState
import at.aau.serg.websocketbrokerdemo.network.game.GameHandler
import android.content.Intent
class GameActivity : ComponentActivity() {
    private lateinit var rootLayout: ViewGroup
    private lateinit var boardImage: ImageView
    private lateinit var gridOverlay: ViewGroup

    private lateinit var checklistOverlay: ViewGroup
    private lateinit var characterPanel: android.widget.LinearLayout

    private lateinit var dialogOverlay: ViewGroup

    private val playerDots = mutableMapOf<String, View>()
    private val characterHighlights = mutableMapOf<String, View>()
    private var currentRoomId: String? = null
    private var hiddenWayUsed = false
    private var boardSetupDone = false

    private lateinit var btnRollDice: Button
    private lateinit var btnHiddenWay: Button
    private lateinit var btnSuggest: Button
    private lateinit var btnAccuse: Button
    private lateinit var btnLeave: Button

    private val playerStatusViews = mutableMapOf<String, TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        rootLayout = findViewById(R.id.rootGameLayout)
        boardImage = findViewById(R.id.imgBoard)
        gridOverlay = findViewById(R.id.gridOverlay)
        checklistOverlay = findViewById(R.id.checklistOverlay)
        characterPanel = findViewById(R.id.characterPanel)

        dialogOverlay = findViewById(R.id.dialogOverlay)

        setupGameHandlers()
        initializePlayerPositions()

        btnRollDice = findViewById(R.id.btnRollDice)
        btnHiddenWay = findViewById(R.id.btnHiddenWay)
        btnSuggest = findViewById(R.id.btnSuggest)
        btnAccuse = findViewById(R.id.btnAccuse)
        btnLeave = findViewById(R.id.btnLeave)

        findViewById<Button>(R.id.btnRollDice).setOnClickListener { onRollDice() }
        findViewById<Button>(R.id.btnHiddenWay).setOnClickListener { onHiddenWay() }
        findViewById<Button>(R.id.btnSuggest).setOnClickListener { onSuggest() }
        findViewById<Button>(R.id.btnAccuse).setOnClickListener { onAccuse() }
        findViewById<Button>(R.id.btnLeave).setOnClickListener { onLeaveGame() }

        boardImage.post {
            if (!boardSetupDone) {
                boardSetupDone = true
                setupBoard()
            }
        }
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

        gridOverlay.removeAllViews()

        for (row in 0 until BoardConfig.ROWS) {
            for (col in 0 until BoardConfig.COLS) {
                val cell = View(this)
                val clp =
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(cellW, cellH)
                        .apply {
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
        val roomOverlay = findViewById<ViewGroup>(R.id.roomOverlay)
        roomOverlay?.removeAllViews()

        setupCharacterPanel(imgY, imgH)
        updateChecklist()
        updateCurrentPlayerHighlight()
        updateButtonStates()
        placeAllPlayerDots()
    }

    private fun setupCharacterPanel(imgY: Int, imgH: Int) {
        characterPanel.removeAllViews()
        characterHighlights.clear()
        playerStatusViews.clear()
        characterPanel.setBackgroundColor(Color.argb(120, 0, 0, 0))
        characterPanel.visibility = View.VISIBLE

        val myId = ClientState.playerId
        val sortedPlayers = ClientState.players.sortedWith(compareBy {
            if (it.playerId == myId) 0 else 1
        })

        for (player in sortedPlayers) {
            val charType = player.character ?: continue

            val itemView =
                layoutInflater.inflate(R.layout.player_panel_entry, characterPanel, false)

            val imageResId = when (charType) {
                "drred" -> R.drawable.cdrred
                "drblue" -> R.drawable.cdrblue
                "mrspink" -> R.drawable.cmrspink
                "mrslavender" -> R.drawable.cmrslavender
                else -> android.R.drawable.ic_menu_help
            }
            itemView.findViewById<ImageView>(R.id.imgCharacter).setImageResource(imageResId)

            val highlightView = itemView.findViewById<View>(R.id.viewActiveHighlight)
            val border = GradientDrawable()
            border.setStroke(GameUIHelper.dpToPx(this, 3), Color.parseColor("#D12E7D"))
            border.cornerRadius = GameUIHelper.dpToPx(this, 4).toFloat()
            border.setColor(Color.TRANSPARENT)
            highlightView.background = border
            highlightView.visibility = View.GONE
            characterHighlights[player.playerId] = highlightView

            playerStatusViews[player.playerId] = itemView.findViewById(R.id.tvStatus)
            characterPanel.addView(itemView)
        }
    }

    private fun initializePlayerPositions() {
        val players = ClientState.players
        players.forEach { player ->
            val charType = ClientState.playerCharacterMap[player.playerId] ?: player.character
            val startPos = charType?.let { BoardConfig.CHARACTER_START_POSITIONS[it] }
            if (startPos != null) {
                ClientState.playerPositions[player.playerId] =
                    "${startPos.first},${startPos.second}"
            } else {
                ClientState.playerPositions[player.playerId] = "6,4"
            }
        }
    }

    private fun onCellTapped(col: Int, row: Int) {
        if (!isMyTurn() || ClientState.isEliminated) return

        val currentPosStr = ClientState.playerPositions[ClientState.playerId]
        if (currentPosStr != null && currentPosStr.contains(",")) {
            val parts = currentPosStr.split(",")
            val c = parts[0].trim().toIntOrNull() ?: 0
            val r = parts[1].trim().toIntOrNull() ?: 0
            if (!BoardConfig.isAdjacent(c, r, col, row)) {
                Toast.makeText(this, getString(R.string.move_not_adjacent), Toast.LENGTH_SHORT)
                    .show()
                return
            }
        }

        if (!BoardConfig.isWalkable(col, row)) {
            Toast.makeText(this, getString(R.string.move_not_walkable), Toast.LENGTH_SHORT).show()
            return
        }

        val phase = ClientState.currentPhase

        // Problem 3 Fix: Do not auto-enter room. Send move command to door field.
        if (phase == "WAITING_FOR_MOVE" && ClientState.remainingMoves > 0) {
            MyStomp.instance.move("$col,$row")
        }
    }

    private fun onRollDice() {
        if (!isMyTurn() || ClientState.isEliminated) return
        if (ClientState.currentPhase != "WAITING_FOR_ROLL") return
        MyStomp.instance.rollDice()
    }

    private fun onHiddenWay() {
        if (!isMyTurn() || ClientState.isEliminated) return
        val pos = ClientState.playerPositions[ClientState.playerId] ?: ""
        if (ClientState.currentPhase != "IN_ROOM" && ClientState.currentPhase != "WAITING_FOR_ROLL") return
        if (BoardConfig.HIDDEN_PASSAGES.containsKey(pos)) {
            MyStomp.instance.takeHiddenWay()
        } else {
            Toast.makeText(this, getString(R.string.no_hidden_passage), Toast.LENGTH_SHORT).show()
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

    private fun onLeaveGame() {
        MyStomp.instance.leaveLobby()
        MyStomp.instance.disconnect()
        finish()
    }

    private fun setupGameHandlers() {
        GameHandler.onRollDice = { value, newPosition ->
            runOnUiThread {
                Toast.makeText(this, getString(R.string.dice_result, value), Toast.LENGTH_SHORT)
                    .show()
                hiddenWayUsed = false
                if (newPosition != null) {
                    updatePlayerDot(ClientState.playerId, newPosition)
                }
                updateButtonStates()
                updateAllPlayerStatuses()
            }
        }

        GameHandler.onMove = { playerId, position, movesLeft ->
            runOnUiThread {
                // Update ALL players' dots (not just mine)
                updatePlayerDot(playerId, position)

                // Only update local room tracking for THIS player
                if (playerId == ClientState.playerId) {
                    val parts = position.split(",")
                    if (parts.size == 2) {
                        val col = parts[0].trim().toIntOrNull() ?: -1
                        val row = parts[1].trim().toIntOrNull() ?: -1
                        val room = BoardConfig.getRoomAtDoor(col, row)

                        if (room != null) {
                            // Problem 3 Fix: Show dialog to enter room on door field
                            android.app.AlertDialog.Builder(this)
                                .setTitle("Enter Room?")
                                .setMessage("Do you want to enter the $room?")
                                .setPositiveButton("Yes") { _, _ ->
                                    MyStomp.instance.enterRoom(room)
                                }
                                .setNegativeButton("No") { _, _ -> }
                                .show()
                        }
                    }
                }
                updateButtonStates()
                updateAllPlayerStatuses()
            }
        }

        GameHandler.onEndTurn = { newIndex ->
            runOnUiThread {
                ClientState.currentPlayerIndex = newIndex
                hiddenWayUsed = false
                currentRoomId = null
                updateCurrentPlayerHighlight()
                updateButtonStates()
                updateAllPlayerStatuses()

                // Only show toast, do NOT trigger "your turn" UI for other players
                val currentPlayer = ClientState.players.getOrNull(newIndex)
                val msg = if (currentPlayer?.playerId == ClientState.playerId)
                    getString(R.string.your_turn) else getString(
                    R.string.turn_other,
                    currentPlayer?.character ?: "Player ${newIndex + 1}"
                )
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }

        GameHandler.onEnterRoom = { playerId, roomId ->
            runOnUiThread {
                // Only update local room for THIS player
                if (playerId == ClientState.playerId) {
                    currentRoomId = roomId
                }
                updatePlayerDot(playerId, roomId)
                updateButtonStates()
                updateAllPlayerStatuses()
            }
        }

        GameHandler.onHiddenWay = { playerId, targetRoom ->
            runOnUiThread {
                // Only update local state for THIS player
                if (playerId == ClientState.playerId) {
                    currentRoomId = targetRoom
                    hiddenWayUsed = true
                }
                updatePlayerDot(playerId, targetRoom)
                updateButtonStates()
                updateAllPlayerStatuses()
            }
        }

        GameHandler.onSuggestionResult = { suggesterID, suspect, room, weapon, matchingCards ->
            runOnUiThread {
                GameUIHelper.showSuggestionTimer(this, rootLayout) {
                    // Only show matching cards to the SUGGESTER
                    if (suggesterID == ClientState.playerId) {
                        if (matchingCards.isNotEmpty()) {
                            GameUIHelper.showResultCards(this, rootLayout, matchingCards)
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.no_matching_cards),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        updateChecklist()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.suggestion_made, "${suggesterID.take(8)}..."),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        GameHandler.onAccusation = { accuserID, suspect, room, weapon, correct, eliminated ->
            runOnUiThread {
                // All players see the accusation cards
                GameUIHelper.showResultCards(this, rootLayout, listOf(suspect, weapon, room), 3000)
                if (correct) {
                    val msg =
                        if (accuserID == ClientState.playerId) getString(R.string.you_won) else getString(
                            R.string.player_won,
                            "${accuserID.take(8)}..."
                        )
                    android.os.Handler(mainLooper).postDelayed({
                        GameUIHelper.showGameEndOverlay(this, rootLayout, msg)
                    }, 3500)
                } else if (eliminated) {
                    // Only show elimination message, differentiate by playerId
                    if (accuserID == ClientState.playerId) {
                        Toast.makeText(
                            this,
                            getString(R.string.wrong_accusation),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.player_eliminated, "${accuserID.take(8)}..."),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        GameHandler.onGameFinished = { winner ->
            runOnUiThread {
                val msg =
                    if (winner == ClientState.playerId) getString(R.string.you_won) else getString(
                        R.string.player_won,
                        "${winner.take(8)}..."
                    )
                GameUIHelper.showGameEndOverlay(this, rootLayout, msg)
            }
        }

        GameHandler.onGameAborted = { reason ->
            runOnUiThread {
                Toast.makeText(this, reason, Toast.LENGTH_LONG).show()
                val intent = Intent(this, LobbyActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
        GameHandler.onGamePaused = { disconnectedId, countdown ->
            runOnUiThread {
                Toast.makeText(this,
                    "player disconnected! $countdown Sekunden zum Rejoin",
                    Toast.LENGTH_LONG).show()
            }
        }

        GameHandler.onContinueGame = { rejoinedId ->
            runOnUiThread {
                Toast.makeText(this,
                    "player back! Game resumed.",
                    Toast.LENGTH_SHORT).show()
            }
        }

        GameHandler.onGameAborted = { reason ->
            runOnUiThread {
                GameUIHelper.showGameEndOverlay(
                    this,
                    rootLayout,
                    getString(R.string.game_over, reason)
                )
            }
        }
    }

    private fun updatePlayerDot(playerId: String, position: String) {
        if (!boardSetupDone) return
        val gridW = gridOverlay.width
        val gridH = gridOverlay.height
        if (gridW == 0 || gridH == 0) return

        val cellW = gridW / BoardConfig.COLS
        val cellH = gridH / BoardConfig.ROWS

        // Remove old dot from wherever it is
        playerDots[playerId]?.let {
            (it.parent as? ViewGroup)?.removeView(it)
        }

        val charType = ClientState.playerCharacterMap[playerId]
            ?: ClientState.players.find { it.playerId == playerId }?.character
        val color = BoardConfig.CHARACTER_COLORS[charType] ?: Color.GRAY
        val dot = GameUIHelper.createPlayerDot(this, color)
        val dotSize = GameUIHelper.dpToPx(this, 16)

        if (position.contains(",")) {
            val parts = position.split(",")
            val col = parts[0].trim().toIntOrNull() ?: 0
            val row = parts[1].trim().toIntOrNull() ?: 0
            val dlp =
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(dotSize, dotSize)
                    .apply {
                        startToStart = androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
                        topToTop = androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
                        leftMargin = col * cellW + (cellW - dotSize) / 2
                        topMargin = row * cellH + (cellH - dotSize) / 2
                    }
            dot.layoutParams = dlp
            gridOverlay.addView(dot)
        } else {
            // Problem 4 Fix: Room position using roomOverlay
            val roomOverlay = findViewById<ViewGroup>(R.id.roomOverlay) ?: return

            val playersInRoom = ClientState.playerPositions.filter { it.value == position }
            val slotIndex = playersInRoom.keys.toList().indexOf(playerId).coerceIn(0, 3)

            val percent = BoardConfig.ROOM_CENTERS_PERCENT[position] ?: Pair(0.5f, 0.5f)
            val centerX = (roomOverlay.width * percent.first).toInt()
            val centerY = (roomOverlay.height * percent.second).toInt()

            val offsetX = (slotIndex % 2) * dotSize
            val offsetY = (slotIndex / 2) * dotSize

            val dlp =
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(dotSize, dotSize)
                    .apply {
                        startToStart = androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
                        topToTop = androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
                        leftMargin = centerX + offsetX - dotSize / 2
                        topMargin = centerY + offsetY - dotSize / 2
                    }
            dot.layoutParams = dlp
            roomOverlay.addView(dot)
        }

        playerDots[playerId] = dot
    }

    private fun placeAllPlayerDots() {
        for ((playerId, position) in ClientState.playerPositions) {
            updatePlayerDot(playerId, position)
        }
    }

    private fun updateChecklist() {
        checklistOverlay.post {
            GameUIHelper.buildChecklistOverlay(
                this, checklistOverlay,
                checklistOverlay.width, checklistOverlay.height
            )
        }
    }

    private fun updateCurrentPlayerHighlight() {
        val players = ClientState.players
        val currentPid = if (players.isNotEmpty() && ClientState.currentPlayerIndex < players.size)
            players[ClientState.currentPlayerIndex].playerId else ""

        characterHighlights.forEach { (pid, view) ->
            view.visibility = if (pid == currentPid) View.VISIBLE else View.GONE
        }
    }

    private fun updateButtonStates() {
        val myTurn = isMyTurn() && !ClientState.isEliminated
        val phase = ClientState.currentPhase
        val myPos = ClientState.playerPositions[ClientState.playerId] ?: ""
        val inRoom = BoardConfig.ROOM_CENTERS_PERCENT.containsKey(myPos)
        val hasHiddenPassage = BoardConfig.HIDDEN_PASSAGES.containsKey(myPos)

        setButtonActive(btnRollDice, myTurn && phase == "WAITING_FOR_ROLL")
        setButtonActive(
            btnHiddenWay,
            myTurn && hasHiddenPassage && (phase == "IN_ROOM" || phase == "WAITING_FOR_ROLL")
        )
        setButtonActive(
            btnSuggest,
            myTurn && inRoom && (phase == "IN_ROOM" || phase == "WAITING_FOR_ROLL")
        )
        setButtonActive(
            btnAccuse,
            myTurn && inRoom && (phase == "IN_ROOM" || phase == "WAITING_FOR_ROLL")
        )
        setButtonActive(btnLeave, true)
    }

    private fun setButtonActive(btn: Button, active: Boolean) {
        btn.alpha = if (active) 1.0f else 0.4f
        btn.isClickable = active
    }

    private fun updateAllPlayerStatuses() {
        val players = ClientState.players
        val currentPid = players.getOrNull(ClientState.currentPlayerIndex)?.playerId ?: ""

        for (player in players) {
            val tv = playerStatusViews[player.playerId] ?: continue

            tv.text = when {
                ClientState.eliminatedPlayers.contains(player.playerId) ->
                    getString(R.string.status_eliminated)

                player.playerId == currentPid -> {
                    val moves = ClientState.remainingMoves
                    when (ClientState.currentPhase) {
                        "WAITING_FOR_ROLL" -> getString(R.string.status_roll)
                        "WAITING_FOR_MOVE" -> getString(R.string.status_moving, moves)
                        "IN_ROOM" -> getString(R.string.status_in_room)
                        "WAITING_FOR_SUGGESTION_RESPONSE" -> getString(R.string.status_suggestion)
                        "WAITING_FOR_LIAR_DECISION" -> getString(R.string.status_liar)
                        "TURN_ENDED" -> getString(R.string.status_waiting)
                        else -> ""
                    }
                }

                else -> getString(R.string.status_waiting)
            }
        }
    }
}