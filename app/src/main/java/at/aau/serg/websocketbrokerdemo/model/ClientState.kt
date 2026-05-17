package at.aau.serg.websocketbrokerdemo.model

import at.aau.serg.websocketbrokerdemo.messaging.dtos.ExistingPlayerDTO

object ClientState {
    lateinit var gameStatus: String
    var availableCharacters: List<String> = emptyList()

    // Player
    var playerId: String = ""
    var myCards: List<String> = emptyList()
    var myCharacter: String? = null

    // Checklist
    var seenCards: MutableSet<String> = mutableSetOf()

    // Spielerliste
    var players: List<ExistingPlayerDTO> = emptyList()

    // Gamestate
    var currentPlayerId: String = ""

    var remainingMoves: Int = 0
    var playerPositions: MutableMap<String, String> = mutableMapOf()
     var currentPhase: String = ""
    var currentPlayerIndex: Int = 0

    var isEliminated: Boolean = false
    var eliminatedPlayers: MutableSet<String> = mutableSetOf()
    var playerCharacterMap: MutableMap<String, String> = mutableMapOf()
}
