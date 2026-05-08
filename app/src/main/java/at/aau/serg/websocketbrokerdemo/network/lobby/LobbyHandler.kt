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
    fun handle(msg: String) {
        val json = JSONObject(msg)
        val type = LobbyMessageType.valueOf(json.getString("type"))
        val payload = json.getJSONObject("payload")

        when (type) {
            LobbyMessageType.NEW_PLAYER_JOINED -> {
                val dto = parseNewPlayerJoined(payload)
                ClientState.players = dto.existingPlayers   // zwischenspeichern
                ClientState.availableCharacters = dto.availableCharacters
                onLobbyJoined?.invoke()
                onNewPlayerJoined?.invoke(dto)              // Activity benachrichtigen
            }
            LobbyMessageType.PLAYER_REJOINED -> {
                val dto = parsePlayerRejoined(payload)
                ClientState.players = dto.existingPlayers
                onLobbyJoined?.invoke()
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
            // setready
            LobbyMessageType.SET_CHARACTER_TYPE_AND_STATUS_READY -> {
            /*
                val dto =parseNewPlayerJoined(payload)
                ClientState.players = dto.existingPlayers
                ClientState.availableCharacters = dto.availableCharacters
                onSetReady?.invoke(dto)

            */
                val dto = parseSetReady(payload)

                ClientState.players = dto.existingPlayers
                ClientState.availableCharacters = dto.availableCharacters

                onSetReady?.invoke(dto)
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
        return PlayerRejoinedPayload(
            playerId = payload.getString("playerId"),
            existingPlayers = parsePlayers(payload)
        )
    }

    private fun parseLobbyError(payload: JSONObject): GameFullPayload {
        return GameFullPayload(
            playerId = payload.getString("playerId"),
            message = payload.getString("message")
        )
    }

    private fun parsePlayers(payload: JSONObject): List<ExistingPlayerDTO> {
        val array = payload.getJSONArray("existingPlayers")
        return (0 until array.length()).map {
            val p = array.getJSONObject(it)
            ExistingPlayerDTO(
                playerId = p.getString("playerId"),
                ready = p.getBoolean("ready"),
                character = p.optString("character").takeIf { it.isNotEmpty() },
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