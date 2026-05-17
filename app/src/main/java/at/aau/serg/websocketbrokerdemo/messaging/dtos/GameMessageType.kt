package at.aau.serg.websocketbrokerdemo.messaging.dtos

enum class GameMessageType {
    ROLL_DICE,
    END_TURN,
    MOVE,
    ENTER_ROOM,
    TAKE_HIDDEN_WAY,
    MAKE_ACCUSATION,
    MAKE_SUGGESTION,
    SUGGESTION_RESULT,
    SUGGESTION_ERROR,
    GAME_FINISHED,
    GAME_PAUSED,
    CONTINUE_GAME,
    GAME_ABORTED


}