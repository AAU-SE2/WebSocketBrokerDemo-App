package at.aau.serg.websocketbrokerdemo.network.game

import android.util.Log
import at.aau.serg.websocketbrokerdemo.messaging.dtos.GameMessageType
import at.aau.serg.websocketbrokerdemo.model.ClientState
import at.aau.serg.websocketbrokerdemo.messaging.dtos.ExistingPlayerDTO
import org.json.JSONObject

class GameHandler {
    companion object {
        var onRollDice: ((Int, String?) -> Unit)? = null
        var onMove: ((String, String, Int) -> Unit)? = null
        var onEndTurn: ((Int) -> Unit)? = null
        var onEnterRoom: ((String, String) -> Unit)? = null
        var onHiddenWay: ((String,String) -> Unit)? = null
        var onAccusation: ((String, String, String, String, Boolean, Boolean) -> Unit)? = null
        var onSuggestionResult: ((String, String, String, String, List<String>) -> Unit)? = null
        var onGameFinished: ((String) -> Unit)? = null
        var onGameAborted: ((String) -> Unit)? = null
        var onGamePaused: ((String, Int) -> Unit)? = null
        var onContinueGame: ((String) -> Unit)? = null
        fun handle(msg: String) {
            try {
                val json = JSONObject(msg)
                val type = json.getString("type")
                val payload = json.optJSONObject("payload")

                val phase = if (payload != null) payload.optString("currentPhase", "") else ""
                if (phase.isNotEmpty()) {
                    ClientState.currentPhase = phase
                }


                when (type) {
                    GameMessageType.ROLL_DICE.name -> {
                        if (payload == null)
                            return
                        val value = payload.getInt("value")
                        ClientState.remainingMoves = value

                        var newPos: String? = null
                        if (payload.has("newPosition")) {
                            newPos = payload.getString("newPosition")
                            val playerId = payload.optString("playerId", ClientState.playerId)
                            ClientState.playerPositions[playerId] = newPos
                        }
                        onRollDice?.invoke(value, newPos)
                    }

                    GameMessageType.MOVE.name -> {
                        if (payload == null)
                            return
                        val playerId = payload.getString("playerId")
                        val position = payload.getString("position")
                        val movesLeft = payload.optInt("movesLeft", 0)
                        ClientState.remainingMoves = movesLeft
                        ClientState.playerPositions[playerId] = position
                        onMove?.invoke(playerId, position, movesLeft)
                    }

                    GameMessageType.END_TURN.name -> {
                        if (payload == null)
                            return
                        val currentPlayerIndex = payload.getInt("currentPlayerIndex")
                        ClientState.currentPlayerIndex = currentPlayerIndex
                        ClientState.remainingMoves = 0
                        onEndTurn?.invoke(currentPlayerIndex)
                    }

                    GameMessageType.ENTER_ROOM.name -> {
                        if (payload == null)
                            return
                        val playerId = payload.getString("playerId")
                        val roomId = payload.getString("roomId")
                        ClientState.playerPositions[playerId] = roomId
                        onEnterRoom?.invoke(playerId, roomId)
                    }

                    GameMessageType.TAKE_HIDDEN_WAY.name -> {
                        if (payload == null)
                            return
                        val playerId = payload.getString("playerId")
                        val targetRoom = payload.getString("targetRoom")
                        ClientState.playerPositions[playerId] = targetRoom
                        onHiddenWay?.invoke(playerId, targetRoom)
                    }

                    GameMessageType.MAKE_ACCUSATION.name -> {
                        if (payload == null)
                            return
                        val accuserID = payload.getString("accuserID")
                        val suspect = payload.getString("suspect")
                        val room = payload.getString("room")
                        val weapon = payload.getString("weapon")
                        val correct = payload.getBoolean("correct")
                        val eliminated = payload.optBoolean("eliminated", false)

                        if (eliminated) {
                            ClientState.eliminatedPlayers.add(accuserID)
                        }
                        if (accuserID == ClientState.playerId && eliminated) {
                            ClientState.isEliminated = true
                        }
                        onAccusation?.invoke(accuserID, suspect, room, weapon, correct, eliminated)
                    }

                    GameMessageType.SUGGESTION_RESULT.name -> {
                        if (payload == null)
                            return
                        val suggesterID = payload.getString("suggesterID")
                        val suspect = payload.getString("suspect")
                        val room = payload.getString("room")
                        val weapon = payload.getString("weapon")

                        val matchingCards = mutableListOf<String>()
                        val cardsArray = payload.optJSONArray("matchingCards")
                        if (cardsArray != null) {
                            for (i in 0 until cardsArray.length()) {
                                val cardObj = cardsArray.getJSONObject(i)
                                val cardName = cardObj.getString("name")
                                matchingCards.add(cardName)
                                // Auto-mark as seen
                                if (suggesterID == ClientState.playerId) {
                                    ClientState.seenCards.add(cardName)
                                }
                            }
                        }
                        onSuggestionResult?.invoke(suggesterID, suspect, room, weapon, matchingCards)
                    }

                    GameMessageType.GAME_FINISHED.name -> {
                        val winner = if (payload != null) payload.optString("winner", "") else ""
                        onGameFinished?.invoke(winner)
                    }
                    GameMessageType.GAME_PAUSED.name -> {
                        val disconnectedId = payload?.optString("disconnectedPlayerId") ?: ""
                        val countdown = payload?.optInt("countdown") ?: 30
                        onGamePaused?.invoke(disconnectedId, countdown)
                    }

                    GameMessageType.CONTINUE_GAME.name -> {
                        val rejoinedId = payload?.optString("rejoinedPlayerId") ?: ""
                        onContinueGame?.invoke(rejoinedId)
                    }

                    GameMessageType.GAME_ABORTED.name -> {
                        val reason = payload?.optString("reason", "Game aborted") ?: "Game aborted"
                        ClientState.gameStatus = "LOBBY"

                        onGameAborted?.invoke(reason)
                    }

                    else -> {
                        Log.w("GameHandler", "Unhandled game message type: $type")
                    }

                }
            } catch (e: Exception) {
                Log.e("GameHandler", "Error handling message", e)
            }
        }
    }
}