package at.aau.serg.websocketbrokerdemo.messaging.dtos

data class ExistingPlayerDTO(
    val playerId: String,
    var ready: Boolean,
    var character: String?,
    var position: String?
)
