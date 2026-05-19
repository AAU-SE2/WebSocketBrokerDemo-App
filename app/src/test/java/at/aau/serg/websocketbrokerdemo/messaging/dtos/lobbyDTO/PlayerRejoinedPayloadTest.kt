package at.aau.serg.websocketbrokerdemo.messaging.dtos.lobbyDTO

import at.aau.serg.websocketbrokerdemo.messaging.dtos.ExistingPlayerDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PlayerRejoinedPayloadTest {

    @Test
    fun `PlayerRejoinedPayload stores values correctly`() {
        val existing = listOf(ExistingPlayerDTO("p2", false, "Mustard", null))
        val payload = PlayerRejoinedPayload("p1", existing)
        Assertions.assertEquals("p1", payload.playerId)
        Assertions.assertEquals(existing, payload.existingPlayers)
    }

    @Test
    fun `PlayerRejoinedPayload equals and hashCode`() {
        val existing = listOf(ExistingPlayerDTO("p2", false, "Mustard", null))
        val a = PlayerRejoinedPayload("p1", existing)
        val b = PlayerRejoinedPayload("p1", existing)
        Assertions.assertEquals(a, b)
        Assertions.assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `PlayerRejoinedPayload copy`() {
        val original = PlayerRejoinedPayload("p1", emptyList())
        val copy = original.copy(playerId = "p99")
        Assertions.assertEquals("p99", copy.playerId)
    }

    @Test
    fun `PlayerRejoinedPayload toString contains values`() {
        val payload = PlayerRejoinedPayload("p1", emptyList())
        Assertions.assertTrue(payload.toString().contains("p1"))

    }
}