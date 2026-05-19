package at.aau.serg.websocketbrokerdemo.messaging.dtos

import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LobbyMessageTest {

    @Test
    fun `LobbyMessage stores values correctly`() {
        val payload = JSONObject("""{"key": "value"}""")
        val msg = LobbyMessage(42, payload)
        Assertions.assertEquals(42, msg.playerId)
        Assertions.assertEquals(payload, msg.payload)
    }

    @Test
    fun `LobbyMessage copy`() {
        val payload = JSONObject("""{"key": "value"}""")
        val msg = LobbyMessage(42, payload)
        val copy = msg.copy(playerId = 99)
        Assertions.assertEquals(99, copy.playerId)
    }

    @Test
    fun `LobbyMessage equals and hashCode`() {
        val payload = JSONObject("""{"key": "value"}""")
        val a = LobbyMessage(42, payload)
        val b = LobbyMessage(42, payload)
        Assertions.assertEquals(a, b)
        Assertions.assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `LobbyMessage toString contains playerId`() {
        val msg = LobbyMessage(42, JSONObject())
        Assertions.assertTrue(msg.toString().contains("42"))
    }
}