package at.aau.serg.websocketbrokerdemo.network.lobby

import at.aau.serg.websocketbrokerdemo.messaging.dtos.ExistingPlayerDTO
import at.aau.serg.websocketbrokerdemo.messaging.dtos.lobbyDTO.GameFullPayload
import at.aau.serg.websocketbrokerdemo.messaging.dtos.LobbyMessage
import at.aau.serg.websocketbrokerdemo.messaging.dtos.LobbyMessageType
import at.aau.serg.websocketbrokerdemo.messaging.dtos.SetreadyDTO
import at.aau.serg.websocketbrokerdemo.messaging.dtos.lobbyDTO.NewPlayerJoinedPayload
import at.aau.serg.websocketbrokerdemo.messaging.dtos.lobbyDTO.PlayerRejoinedPayload
import at.aau.serg.websocketbrokerdemo.model.ClientState
import org.json.JSONObject

object LobbyHandler {

    var onOtherPlayerRemoved: ((String) -> Unit)? = null
    var onNewPlayerJoined: ((NewPlayerJoinedPayload) -> Unit)? = null
    var onPlayerRejoined: ((PlayerRejoinedPayload) -> Unit)? = null
    var onPlayerRejoinedRunning: (() -> Unit)? = null
    var onGameFull: ((GameFullPayload) -> Unit)? = null
    var onLobbyJoined: (() -> Unit)? = null
    var onPlayerRemoved: ((String) -> Unit)? = null
    var onError: ((String) -> Unit)? = null
    var onSetReady: ((SetreadyDTO) -> Unit)? = null
    var onGameStarted: (() -> Unit)? = null
    var onStartGameError: ((String) -> Unit)? = null
    fun handle(msg: String) {
        try {
            val json = JSONObject(msg)
            val typeStr = json.getString("type")
            val type = try {
                LobbyMessageType.valueOf(typeStr)
            } catch (e: IllegalArgumentException) {
                android.util.Log.w("LobbyHandler", "Unknown lobby message type: $typeStr")
                return
            }
            val payload = json.optJSONObject("payload") ?: run {
                android.util.Log.w("LobbyHandler", "Missing payload in message: $typeStr")
                return
            }

            when (type) {
                LobbyMessageType.NEW_PLAYER_JOINED -> {
                    val dto = parseNewPlayerJoined(payload)
                    ClientState.players = dto.existingPlayers
                    ClientState.availableCharacters = dto.availableCharacters

                    if (dto.playerId == ClientState.playerId) {
                        onLobbyJoined?.invoke()
                    }
                    onNewPlayerJoined?.invoke(dto)
                }

                LobbyMessageType.PLAYER_REJOINED -> {
                    val dto = parsePlayerRejoined(payload)
                    ClientState.gameStatus = payload.optString("gameStatus", "LOBBY")
                    ClientState.players = dto.existingPlayers
                    if (dto.availableCharacters.isNotEmpty()) {
                        ClientState.availableCharacters = dto.availableCharacters
                    }
                    onPlayerRejoined?.invoke(dto)
                }

                LobbyMessageType.PLAYER_REJOINED_RUNNING -> {
                    val rejoinedPlayerId = payload.optString("playerId", "")
                    val isMe = rejoinedPlayerId == ClientState.playerId

                    // Only the rejoining player restores their personal state
                    if (isMe) {
                        ClientState.myCharacter = payload.optString("myCharacter").takeIf { it.isNotEmpty() }
                        ClientState.isEliminated = payload.optBoolean("isEliminated", false)

                        val myCardsArray = payload.optJSONArray("myCards")
                        if (myCardsArray != null) {
                            val cardIds = mutableListOf<String>()
                            for (i in 0 until myCardsArray.length()) {
                                cardIds.add(myCardsArray.getJSONObject(i).getString("name"))
                            }
                            ClientState.myCards = cardIds
                            ClientState.seenCards.addAll(cardIds)
                        }

                        ClientState.currentPlayerId = payload.optString("currentPlayerId", "")
                        ClientState.currentPlayerIndex = payload.optInt("currentPlayerIndex", 0)
                        ClientState.currentPhase = payload.optString("currentPhase", "")
                        ClientState.remainingMoves = payload.optInt("remainingMoves", 0)

                        // Restore full state from server
                        val playersArray = payload.optJSONArray("players")
                        if (playersArray != null) {
                            val playerList = mutableListOf<ExistingPlayerDTO>()
                            for (i in 0 until playersArray.length()) {
                                val p = playersArray.getJSONObject(i)
                                playerList.add(ExistingPlayerDTO(
                                    playerId = p.getString("playerId"),
                                    ready = p.optBoolean("ready", false),
                                    character = p.optString("characterType").takeIf { it.isNotEmpty() },
                                    position = p.optString("position").takeIf { it.isNotEmpty() }
                                ))
                            }
                            ClientState.players = playerList
                        }

                        val positions = payload.optJSONObject("playerPositions")
                        if (positions != null) {
                            ClientState.playerPositions.clear()
                            for (key in positions.keys()) {
                                ClientState.playerPositions[key] = positions.getString(key)
                            }
                        }

                        val charMap = payload.optJSONObject("playerCharacterMap")
                        if (charMap != null) {
                            ClientState.playerCharacterMap.clear()
                            for (key in charMap.keys()) {
                                ClientState.playerCharacterMap[key] = charMap.getString(key)
                            }
                        }

                        val eliminated = payload.optJSONArray("eliminatedPlayers")
                        if (eliminated != null) {
                            ClientState.eliminatedPlayers.clear()
                            for (i in 0 until eliminated.length()) {
                                ClientState.eliminatedPlayers.add(eliminated.getString(i))
                            }
                        }

                        onPlayerRejoinedRunning?.invoke()
                    }
                    // Other clients: nothing to do — they already have their own state
                    // The CONTINUE_GAME message (sent separately) will dismiss the pause overlay
                }

                LobbyMessageType.GAME_FULL -> {
                    onGameFull?.invoke(parseLobbyError(payload))
                }

                LobbyMessageType.PLAYER_REMOVED -> {
                    val playerId = payload.getString("playerId")
                    if (playerId == ClientState.playerId) {
                        onPlayerRemoved?.invoke(playerId)
                    } else {
                        onOtherPlayerRemoved?.invoke(playerId)
                    }
                }

                LobbyMessageType.SET_CHARACTER_TYPE_AND_STATUS_READY -> {
                    val dto = parseSetReady(payload)
                    ClientState.players = dto.existingPlayers
                    ClientState.availableCharacters = dto.availableCharacters
                    onSetReady?.invoke(dto)
                }

                LobbyMessageType.GAME_STARTED -> {
                    ClientState.currentPhase = payload.optString("currentPhase", "")
                    ClientState.currentPlayerIndex = payload.optInt("currentPlayerIndex", 0)
                    val playersArray = payload.optJSONArray("players")
                    if (playersArray != null) {
                        for (i in 0 until playersArray.length()) {
                            val playerObj = playersArray.getJSONObject(i)
                            val pid = playerObj.getString("playerId")
                            val existingPlayer = ClientState.players.find { it.playerId == pid }
                            if (existingPlayer?.character != null) {
                                ClientState.playerCharacterMap[pid] = existingPlayer.character!!
                            }
                            if (pid == ClientState.playerId) {
                                val cardsArray = playerObj.optJSONArray("cards")
                                if (cardsArray != null) {
                                    val cardIds = mutableListOf<String>()
                                    for (j in 0 until cardsArray.length()) {
                                        cardIds.add(cardsArray.getJSONObject(j).getString("name"))
                                    }
                                    ClientState.myCards = cardIds
                                    ClientState.seenCards.addAll(cardIds)
                                }
                            }
                        }
                    }
                    onGameStarted?.invoke()
                }

                LobbyMessageType.START_GAME_ERROR -> {
                    val reason = payload.optString("reason", "Game could not be started")
                    onStartGameError?.invoke(reason)
                }

                LobbyMessageType.LEAVE_ERROR -> {
                    val reason = payload.optString("reason", "Could not leave lobby")
                    onError?.invoke(reason)
                }

                LobbyMessageType.SET_READY_ERROR -> {
                    val reason = payload.optString("reason", "Could not set ready status")
                    onError?.invoke(reason)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("LobbyHandler", "Error handling lobby message", e)
        }
    }

    private fun parseNewPlayerJoined(payload: JSONObject): NewPlayerJoinedPayload {
        val characters = (0 until payload.getJSONArray("availableCharacters").length())
            .map { payload.getJSONArray("availableCharacters").getString(it) }
        return NewPlayerJoinedPayload(
            playerId = payload.getString("playerId"),
            availableCharacters = characters,
            existingPlayers = parsePlayers(payload)
        )
    }

    private fun parsePlayerRejoined(payload: JSONObject): PlayerRejoinedPayload {
        val availChars = payload.optJSONArray("availableCharacters")
        val characters = if (availChars != null) {
            (0 until availChars.length()).map { availChars.getString(it) }
        } else emptyList()

        return PlayerRejoinedPayload(
            playerId = payload.getString("playerId"),
            existingPlayers = parsePlayers(payload),
            availableCharacters = characters
        )
    }

    private fun parseLobbyError(payload: JSONObject): GameFullPayload {
        return GameFullPayload(
            playerId = payload.getString("playerId"),
            message = payload.getString("message")
        )
    }

    private fun parsePlayers(payload: JSONObject): List<ExistingPlayerDTO> {
        val array = if (payload.has("existingPlayers"))
            payload.getJSONArray("existingPlayers")
        else
            payload.getJSONArray("players")
        return (0 until array.length()).map {
            val p = array.getJSONObject(it)
            ExistingPlayerDTO(
                playerId = p.getString("playerId"),
                ready = p.optBoolean("ready", false),
                character = p.optString("characterType")
                    .ifEmpty { p.optString("character") }
                    .takeIf { it.isNotEmpty() },
                position = p.optString("position").takeIf { it.isNotEmpty() }
            )
        }
    }

    private fun parseSetReady(payload: JSONObject): SetreadyDTO {
        val characters = (0 until payload.getJSONArray("availableCharacters").length())
            .map { payload.getJSONArray("availableCharacters").getString(it) }

        return SetreadyDTO(
            playerId = payload.getString("playerId"),
            characterType = payload.getString("characterType"),
            ready = payload.getBoolean("ready"),
            availableCharacters = characters,
            existingPlayers = parsePlayers(payload)
        )
    }
}