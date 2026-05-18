package at.aau.serg.websocketbrokerdemo.messaging.dtos

enum class LobbyMessageType {
    NEW_PLAYER_JOINED,
    PLAYER_REJOINED,
    PLAYER_REJOINED_RUNNING,
    GAME_FULL,
    PLAYER_REMOVED,
    SET_CHARACTER_TYPE_AND_STATUS_READY,
    GAME_STARTED,
    START_GAME_ERROR,
    LEAVE_ERROR,
    SET_READY_ERROR
}