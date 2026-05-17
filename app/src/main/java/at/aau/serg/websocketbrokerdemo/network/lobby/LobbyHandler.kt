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
    var onGameFull: ((GameFullPayload) -> Unit)? = null
    var onLobbyJoined: (() -> Unit)? = null
    var onPlayerRemoved: ((String) -> Unit)? = null
    // setready
    //var onSetReady: ((NewPlayerJoinedPayload) -> Unit)? = null
    var onSetReady: ((SetreadyDTO) -> Unit)? = null
    var onGameStarted: (() -> Unit)? = null
    var onStartGameError: ((String) -> Unit)? = null
    fun handle(msg: String) {
        val json = JSONObject(msg)
        val type = LobbyMessageType.valueOf(json.getString("type"))
        val payload = json.getJSONObject("payload")

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
                ClientState.gameStatus = payload.optString("gameStatus", "LOBBY") // ← NEU

                ClientState.players = dto.existingPlayers
                if (dto.availableCharacters.isNotEmpty()) {
                    ClientState.availableCharacters = dto.availableCharacters
                }
/*
                if (dto.playerId == ClientState.playerId) {
                    onLobbyJoined?.invoke()
                }

 */

                onPlayerRejoined?.invoke(dto)
            }
            LobbyMessageType.GAME_FULL -> {
                onGameFull?.invoke(parseLobbyError(payload))
            }
            LobbyMessageType.PLAYER_REMOVED -> {
                val playerId = payload.getString("playerId")
                if (playerId == ClientState.playerId) {
                    onPlayerRemoved?.invoke(playerId)
                } else {
                    onOtherPlayerRemoved?.invoke(playerId)  //       anderer Spieler
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
                                    val cardObj = cardsArray.getJSONObject(j)
                                    cardIds.add(cardObj.getString("name"))
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
        //val array = payload.getJSONArray("existingPlayers")
        val array = if (payload.has("existingPlayers"))
            payload.getJSONArray("existingPlayers")
        else
            payload.getJSONArray("players")
        return (0 until array.length()).map {
            val p = array.getJSONObject(it)
            ExistingPlayerDTO(
                playerId = p.getString("playerId"),
                ready = p.optBoolean("ready", false),

               // ready = p.getBoolean("ready"),
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