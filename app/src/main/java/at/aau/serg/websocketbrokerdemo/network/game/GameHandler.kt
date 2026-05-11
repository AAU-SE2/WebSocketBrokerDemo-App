package at.aau.serg.websocketbrokerdemo.network.game

import android.util.Log
import at.aau.serg.websocketbrokerdemo.messaging.dtos.GameMessageType
import at.aau.serg.websocketbrokerdemo.model.ClientState
import org.json.JSONObject

class GameHandler {
    companion object {
        var onRollDice: ((Int) -> Unit)? = null
        var onMove: ((String, String) -> Unit)? = null
        var onEndTurn: ((Int) -> Unit)? = null
        var onEnterRoom: ((String) -> Unit)? = null
        var onHiddenWay: (() -> Unit)? = null
        var onAccusation: ((String) -> Unit)? = null
        var onSuggestion: ((String) -> Unit)? = null
        fun handle(msg: String) {

            try {
                val json = JSONObject(msg)
                val type = GameMessageType.valueOf(json.getString("type"))
                val payload = json.optJSONObject("payload")



                when (type) {
                    GameMessageType.ROLL_DICE -> {
                        val value = payload?.getInt("value") ?: return
                        onRollDice?.invoke(value)
                    }

                    GameMessageType.MOVE -> {
                        val playerId = payload?.getString("playerId") ?: return
                        val position = payload.getString("position")
                        onMove?.invoke(playerId, position)
                    }

                    GameMessageType.END_TURN -> {
                        val currentPlayerIndex = payload?.getInt("currentPlayerIndex") ?: return

                        ClientState.currentPlayerIndex = currentPlayerIndex

                        onEndTurn?.invoke(currentPlayerIndex)
                    }

                    GameMessageType.ENTER_ROOM -> {
                        val roomId = payload?.getString("roomId") ?: return
                        onEnterRoom?.invoke(roomId)
                    }

                    GameMessageType.TAKE_HIDDEN_WAY -> {
                        onHiddenWay?.invoke()
                    }

                    GameMessageType.MAKE_ACCUSATION -> {
                        val raw = payload?.toString() ?: return
                        onAccusation?.invoke(raw)
                    }

                    GameMessageType.MAKE_SUGGESTION -> {
                        val raw = payload?.toString() ?: return
                        onSuggestion?.invoke(raw)
                    }
                    GameMessageType.SUGGESTION_RESULT,
                    GameMessageType.SUGGESTION_ERROR,
                    GameMessageType.GAME_FINISHED,
                    GameMessageType.GAME_ABORTED -> Unit

                }
            } catch (e: Exception) {
                Log.e("GameHandler", "Error handling message", e)
            }
        }
    }
}