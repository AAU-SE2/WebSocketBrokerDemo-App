package at.aau.serg.websocketbrokerdemo.messaging.dtos

import org.json.JSONObject

data class LobbyMessage(val playerId: Int, var payload: JSONObject)