import android.os.Handler
import android.os.Looper
import android.util.Log
import at.aau.serg.websocketbrokerdemo.Callbacks
import at.aau.serg.websocketbrokerdemo.messaging.dtos.LobbyMessageType
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
import java.util.logging.Logger

private const val WEBSOCKET_URI = "ws://10.0.2.2:8080/websocket-example-broker"

class MyStomp(val callbacks: Callbacks) {
    private var lobbyFlow: Flow<String>? = null
    private var lobbyCollector: Job? = null
    private var gameFlow: Flow<String>? = null
    private var gameCollector: Job? = null

    private lateinit var client: StompClient
    //private var session: StompSession? = null

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var activeSession: StompSession

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
                session = activeSession

                Log.d("STOMP", "CONNECTED -> session = $activeSession")
                // connect to topic lobby-response
                lobbyFlow = activeSession.subscribeText("/topic/lobby-response")
                lobbyCollector = scope.launch {
                    lobbyFlow?.collect { msg ->
                        // For Debugging
                        Log.d("MyStomp", "Received lobby-response: $msg")
                        LobbyHandler.handle(msg)
                    }
                }
                callback("connected")

                // connect to topic game-response
                gameFlow = activeSession.subscribeText("/topic/game-response")
                gameCollector = scope.launch {
                    gameFlow?.collect { msg ->
                        // For Debugging
                        Log.d("MyStomp", "Received game-response: $msg")
                        GameHandler.handle(msg)
                    }
                }

                callback("connected")
                val payload = JSONObject()
                payload.put("playerKey", ClientState.playerId)

                val json = JSONObject()
                json.put("type", OutgoingLobbyMessageType.JOIN_LOBBY.toString())
                json.put("payload", payload)
                Log.d("STOMP", "AUTO JOIN -> session = $activeSession")
                activeSession.sendText("/app/lobby", json.toString())

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


    fun leaveLobby() {
        val payload = JSONObject()
        payload.put("playerId", ClientState.playerId)

        val json = JSONObject()
        json.put("type", OutgoingLobbyMessageType.LEAVE_LOBBY.toString())
        json.put("payload", payload)

        scope.launch {
            try {
                session?.sendText("/app/lobby", json.toString())
                    ?: callback("Error: Not connected")
            } catch (e: Exception) {
                Log.e("MyStomp", "Leaving lobby failed", e)
            }
        }
    }
    fun joinLobby() {
        val payload = JSONObject()
        payload.put("playerId", ClientState.playerId)

        val json = JSONObject()
        json.put("type", OutgoingLobbyMessageType.JOIN_LOBBY.toString())
        json.put("payload", payload)

        Log.d("MyStomp", "JOIN_LOBBY payload: $payload")
        Log.d("MyStomp", "JOIN_LOBBY full message: $json")
        scope.launch {
            try{
            session?.sendText("/app/lobby", json.toString())
                ?: callback("Error: Not connected")
        } catch (e: Exception) {
            Log.e("MyStomp", "Join lobby failed", e)
        }
        }
    }
    fun startGame() {
        val json = JSONObject()
        json.put("type", "START_GAME")
        Log.d("MyStomp", "Sending START_GAME: $json")

        scope.launch {
            try {
                session?.sendText("/app/game", json.toString())
                    ?: callback("Error: Not connected")
            } catch (e: Exception) {
                Log.e("MyStomp", "START_GAME failed", e)
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

        scope.launch {
            try {
                session?.sendText("/app/lobby", json.toString())
                    ?: callback("Error: Not connected")
            } catch (e: Exception) {
                Log.e("MyStomp", "SET_READY failed", e)
            }
        }
    }

}