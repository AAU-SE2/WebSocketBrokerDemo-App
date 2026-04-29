package at.aau.serg.websocketbrokerdemo.messaging.dtos

import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GameMessageTest {

    @Test
    fun `GameMessage stores values correctly`() {
        val payload = JSONObject("""{"key": "value"}""")
        val msg = GameMessage(1, payload)
        Assertions.assertEquals(1, msg.type)
        Assertions.assertEquals(payload, msg.payload)
    }

    @Test
    fun `GameMessage copy`() {
        val payload = JSONObject("""{"key": "value"}""")
        val msg = GameMessage(1, payload)
        val copy = msg.copy(type = 2)
        Assertions.assertEquals(2, copy.type)
        Assertions.assertEquals(payload, copy.payload)
    }

    @Test
    fun `GameMessage equals and hashCode`() {
        val payload = JSONObject("""{"key": "value"}""")
        val a = GameMessage(1, payload)
        val b = GameMessage(1, payload)
        Assertions.assertEquals(a, b)
        Assertions.assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `GameMessage toString contains type`() {
        val msg = GameMessage(1, JSONObject())
        Assertions.assertTrue(msg.toString().contains("1"))
    }
}