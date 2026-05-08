package at.aau.serg.websocketbrokerdemo.messaging.dtos

data class SetreadyDTO (

    val playerId: String,
    val characterType: String,
    val ready: Boolean,
    val availableCharacters: List<String>,
    val existingPlayers: List<ExistingPlayerDTO>


    )