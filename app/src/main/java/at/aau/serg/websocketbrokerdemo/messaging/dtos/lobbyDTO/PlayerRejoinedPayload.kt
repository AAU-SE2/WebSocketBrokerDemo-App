package at.aau.serg.websocketbrokerdemo.messaging.dtos.lobbyDTO

import at.aau.serg.websocketbrokerdemo.messaging.dtos.ExistingPlayerDTO

data class PlayerRejoinedPayload(
    val playerId: String,
    val existingPlayers: List<ExistingPlayerDTO>,
    val availableCharacters: List<String> = emptyList(),
    val gameStatus: String = "LOBBY",
    val currentPlayerId: String = "",
    val currentPhase: String = "",
    val myCards: List<String> = emptyList(),
    val characterType: String = ""
)