package at.aau.serg.websocketbrokerdemo.messaging.dtos.lobbyDTO

import at.aau.serg.websocketbrokerdemo.messaging.dtos.ExistingPlayerDTO

data class NewPlayerJoinedPayload(
    val playerId: String,
    val availableCharacters: List<String>,
    val existingPlayers: List<ExistingPlayerDTO>
)