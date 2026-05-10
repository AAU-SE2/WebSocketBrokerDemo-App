package at.aau.serg.websocketbrokerdemo.messaging.dtos

enum class OutgoingLobbyMessageType {
    JOIN_LOBBY,
    LEAVE_LOBBY,
    SET_CHARACTER_TYPE_AND_STATUS_READY,
    START_GAME
}