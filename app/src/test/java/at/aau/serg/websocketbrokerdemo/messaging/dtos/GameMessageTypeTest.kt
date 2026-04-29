package at.aau.serg.websocketbrokerdemo.messaging.dtos

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GameMessageTypeTest {

    @Test
    fun `GameMessageType all values exist`() {
        val values = GameMessageType.values()
        Assertions.assertTrue(values.contains(GameMessageType.ROLL_DICE))
        Assertions.assertTrue(values.contains(GameMessageType.END_TURN))
        Assertions.assertTrue(values.contains(GameMessageType.MOVE))
        Assertions.assertTrue(values.contains(GameMessageType.ENTER_ROOM))
        Assertions.assertTrue(values.contains(GameMessageType.TAKE_HIDDEN_WAY))
        Assertions.assertTrue(values.contains(GameMessageType.MAKE_ACCUSATION))
        Assertions.assertTrue(values.contains(GameMessageType.MAKE_SUGGESTION))
    }

    @Test
    fun `GameMessageType valueOf`() {
        Assertions.assertEquals(GameMessageType.ROLL_DICE, GameMessageType.valueOf("ROLL_DICE"))
        Assertions.assertEquals(GameMessageType.MOVE, GameMessageType.valueOf("MOVE"))
    }
}