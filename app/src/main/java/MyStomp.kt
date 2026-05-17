import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.websocketbrokerdemo.Callbacks
import at.aau.serg.websocketbrokerdemo.messaging.dtos.OutgoingLobbyMessageType
import at.aau.serg.websocketbrokerdemo.model.ClientState
import at.aau.serg.websocketbrokerdemo.network.game.GameHandler
import at.aau.serg.websocketbrokerdemo.network.lobby.LobbyHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient
import org.json.JSONObject

private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/websocket-example-broker"
private const val LOBBY_DESTINATION = "/app/lobby"
private const val GAME_DESTINATION = "/app/game"
class MyStomp(val callbacks: Callbacks) {
    private var lobbyFlow: Flow<String>? = null
    private var lobbyCollector: Job? = null
    private var gameFlow: Flow<String>? = null
    private var gameCollector: Job? = null

    private lateinit var client: StompClient

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var activeSession: StompSession

    private var connected = false
    companion object {
        lateinit var instance: MyStomp
    }

    init {
        instance = this
    }

    fun connect() {
        Log.d("STOMP", "CONNECT called")
        client = StompClient(OkHttpWebSocketClient()) // other config can be passed in here
        scope.launch {
            try {
                activeSession = client.connect(WEBSOCKET_URI)

                Log.d("STOMP", "CONNECTED -> session = $activeSession")
                // connect to topic lobby-response
                lobbyFlow = activeSession.subscribeText("/topic/lobby-response")
               /*
                lobbyCollector = scope.launch {
                    lobbyFlow?.collect { msg ->
                        // For Debugging
                        Log.d("MyStomp", "Received lobby-response: $msg")
                        LobbyHandler.handle(msg)
                    }


                }
                //
                */
                lobbyCollector = scope.launch {
                    try {
                        lobbyFlow?.collect { msg ->
                            Log.d("MyStomp", "Received lobby-response: $msg")
                            LobbyHandler.handle(msg)
                        }
                    } catch (e: Exception) {
                        Log.e("MyStomp", "Lobby connection lost", e)
                        // Nicht crashen! Einfach still beenden
                    }
                }
                callback("connected")

                // connect to topic game-response
                gameFlow = activeSession.subscribeText("/topic/game-response")

                /*
                gameCollector = scope.launch {
                    gameFlow?.collect { msg ->
                        // For Debugging
                        Log.d("MyStomp", "Received game-response: $msg")
                        GameHandler.handle(msg)
                    }
                }


                 */
                gameCollector = scope.launch {
                    try {
                        gameFlow?.collect { msg ->
                            Log.d("MyStomp", "Received game-response: $msg")
                            GameHandler.handle(msg)
                        }
                    } catch (e: Exception) {
                        Log.e("MyStomp", "Game connection lost", e)
                        // Nicht crashen!
                    }
                }
                callback("connected")
                val payload = JSONObject()
                payload.put("playerKey", ClientState.playerId)

                val json = JSONObject()
                json.put("type", OutgoingLobbyMessageType.JOIN_LOBBY.toString())
                json.put("payload", payload)
                Log.d("STOMP", "AUTO JOIN -> session = $activeSession")
                activeSession.sendText(LOBBY_DESTINATION, json.toString())

            } catch (e: Exception) {
                Log.e("MyStomp", "Connection failed", e)
                callback("Connection error")
            }
        }


    }

    private fun callback(msg: String) {
        Handler(Looper.getMainLooper()).post {
            callbacks.onResponse(msg)
        }
    }

    fun disconnect() {
        scope.launch {
            try {
                lobbyCollector?.cancel()
                lobbyCollector = null
                gameCollector?.cancel()
                gameCollector = null
                if (::activeSession.isInitialized) {
                    activeSession.disconnect()
                }
                Log.d("MyStomp", "Disconnected successfully")
            } catch (e: Exception) {
                Log.e("MyStomp", "Disconnect failed", e)
            }
        }
    }


    fun leaveLobby() {
        val payload = JSONObject()
        payload.put("playerId", ClientState.playerId)

        val json = JSONObject()
        json.put("type", OutgoingLobbyMessageType.LEAVE_LOBBY.toString())
        json.put("payload", payload)
        Log.d("STOMP", "LEAVE -> session = $activeSession")

        scope.launch {
            try {
                if (::activeSession.isInitialized) {
                    activeSession.sendText(LOBBY_DESTINATION, json.toString())
                } else {
                    callback("Error: Not connected")
                }
            } catch (e: Exception) {
                Log.e("MyStomp", "Leaving lobby failed", e)
            }
        }
    }
    fun startGame() {
        val payload = JSONObject()
        val json = JSONObject()
        json.put("type", OutgoingLobbyMessageType.START_GAME.toString())
        json.put("payload", payload)
        Log.d("MyStomp", "Sending START_GAME: $json")

        scope.launch {
            try {
                activeSession.sendText(LOBBY_DESTINATION, json.toString())
                    ?: callback("Error: Not connected")
            } catch (e: Exception) {
                Log.e("MyStomp", "START_GAME failed", e)
            }
        }
    }
    fun endTurn() {
        val payload = JSONObject()

        val json = JSONObject()
        json.put("type", "END_TURN")
        json.put("payload", payload)

        Log.d("MyStomp", "Sending END_TURN: $json")

        scope.launch {
            try {
                if (::activeSession.isInitialized) {
                    activeSession.sendText("/app/game", json.toString())
                } else {
                    callback("Error: Not connected")
                }
            } catch (e: Exception) {
                Log.e("MyStomp", "END_TURN failed", e)
            }
        }
    }
    fun setReady(characterType: String, isReady: Boolean) {
        val json = JSONObject()
        json.put("type", "SET_CHARACTER_TYPE_AND_STATUS_READY")

        val payload = JSONObject()
        payload.put("playerId", ClientState.playerId)
        payload.put("characterType", characterType)
        payload.put("ready", isReady)

        json.put("payload", payload)
        Log.d("STOMP", "SET_READY -> session = $activeSession")

        scope.launch {
            try {
                if (::activeSession.isInitialized) {
                    activeSession.sendText(LOBBY_DESTINATION, json.toString())
                } else {
                    callback("Error: Not connected")
                }
            } catch (e: Exception) {
                Log.e("MyStomp", "SET_READY failed", e)
            }
        }
    }

    fun rollDice() {
        sendGameMessage("ROLL_DICE") { payload ->
            payload.put("playerId", ClientState.playerId)
        }
    }

    fun move(position: String) {
        sendGameMessage("MOVE") { payload ->
            payload.put("playerId", ClientState.playerId)
            payload.put("position", position)
        }
    }

    fun enterRoom(roomId: String) {
        sendGameMessage("ENTER_ROOM") { payload ->
            payload.put("playerId", ClientState.playerId)
            payload.put("roomId", roomId)
        }
    }

    fun takeHiddenWay() {
        sendGameMessage("TAKE_HIDDEN_WAY") { payload ->
            payload.put("playerId", ClientState.playerId)
        }
    }


    private fun sendGameMessage(type: String, buildPayload: (JSONObject) -> Unit) {
        val payload = JSONObject()
        buildPayload(payload)

        val json = JSONObject()
        json.put("type", type)
        json.put("payload", payload)

        Log.d("MyStomp", "Sending $type: $json")

        scope.launch {
            try {
                if (::activeSession.isInitialized) {
                    activeSession.sendText(GAME_DESTINATION, json.toString())
                } else {
                    callback("Error: Not connected")
                }
            } catch (e: Exception) {
                Log.e("MyStomp", "$type failed", e)
            }
        }
    }

    fun makeSuggestion(suspect: String, room: String, weapon: String) {
        sendGameMessage("MAKE_SUGGESTION") { payload ->
            payload.put("suggesterID", ClientState.playerId)
            payload.put("suspect", suspect)
            payload.put("room", room)
            payload.put("weapon", weapon)
        }
    }

    fun makeAccusation(suspect: String, room: String, weapon: String) {
        sendGameMessage("MAKE_ACCUSATION") { payload ->
            payload.put("accuserID", ClientState.playerId)
            payload.put("suspect", suspect)
            payload.put("room", room)
            payload.put("weapon", weapon)
        }
    }
}