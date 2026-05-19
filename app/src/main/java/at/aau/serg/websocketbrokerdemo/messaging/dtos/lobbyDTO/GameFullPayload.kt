package at.aau.serg.websocketbrokerdemo.messaging.dtos.lobbyDTO

data class GameFullPayload(
    val playerId: String,
    val message: String
)