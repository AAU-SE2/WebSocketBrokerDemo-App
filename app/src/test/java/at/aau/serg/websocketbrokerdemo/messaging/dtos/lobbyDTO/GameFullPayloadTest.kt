package at.aau.serg.websocketbrokerdemo.messaging.dtos.lobbyDTO

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GameFullPayloadTest {

    @Test
    fun `GameFullPayload stores values correctly`() {
        val payload = GameFullPayload("p1", "Game is full")
        Assertions.assertEquals("p1", payload.playerId)
        Assertions.assertEquals("Game is full", payload.message)
    }

    @Test
    fun `GameFullPayload equals and hashCode`() {
        val a = GameFullPayload("p1", "full")
        val b = GameFullPayload("p1", "full")
        Assertions.assertEquals(a, b)
        Assertions.assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `GameFullPayload copy`() {
        val original = GameFullPayload("p1", "full")
        val copy = original.copy(message = "changed")
        Assertions.assertEquals("p1", copy.playerId)
        Assertions.assertEquals("changed", copy.message)
    }

    @Test
    fun `GameFullPayload toString contains values`() {
        val payload = GameFullPayload("p1", "full")
        Assertions.assertTrue(payload.toString().contains("p1"))
    }
}