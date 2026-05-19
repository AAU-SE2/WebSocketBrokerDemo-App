package at.aau.serg.websocketbrokerdemo.messaging.dtos

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LobbyMessageTypeTest {

    @Test
    fun `LobbyMessageType all values exist`() {
        val values = LobbyMessageType.values()
        Assertions.assertTrue(values.contains(LobbyMessageType.NEW_PLAYER_JOINED))
        Assertions.assertTrue(values.contains(LobbyMessageType.PLAYER_REJOINED))
        Assertions.assertTrue(values.contains(LobbyMessageType.GAME_FULL))
        Assertions.assertTrue(values.contains(LobbyMessageType.PLAYER_REMOVED))
    }

    @Test
    fun `LobbyMessageType valueOf`() {
        Assertions.assertEquals(LobbyMessageType.GAME_FULL, LobbyMessageType.valueOf("GAME_FULL"))
    }
}