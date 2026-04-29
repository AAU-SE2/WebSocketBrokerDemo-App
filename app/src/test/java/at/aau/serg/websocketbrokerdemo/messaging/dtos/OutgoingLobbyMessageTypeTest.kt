package at.aau.serg.websocketbrokerdemo.messaging.dtos

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OutgoingLobbyMessageTypeTest {

    @Test
    fun `OutgoingLobbyMessageType all values exist`() {
        val values = OutgoingLobbyMessageType.values()
        Assertions.assertTrue(values.contains(OutgoingLobbyMessageType.JOIN_LOBBY))
        Assertions.assertTrue(values.contains(OutgoingLobbyMessageType.LEAVE_LOBBY))
    }

    @Test
    fun `OutgoingLobbyMessageType valueOf`() {
        Assertions.assertEquals(OutgoingLobbyMessageType.JOIN_LOBBY, OutgoingLobbyMessageType.valueOf("JOIN_LOBBY"))
    }
}