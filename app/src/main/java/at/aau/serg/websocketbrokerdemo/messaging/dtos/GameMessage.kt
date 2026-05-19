package at.aau.serg.websocketbrokerdemo.messaging.dtos

import org.json.JSONObject

data class GameMessage(var type: Int, var payload: JSONObject)
