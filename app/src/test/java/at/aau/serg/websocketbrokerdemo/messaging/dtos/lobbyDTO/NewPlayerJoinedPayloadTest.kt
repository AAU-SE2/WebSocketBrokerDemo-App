package at.aau.serg.websocketbrokerdemo.messaging.dtos.lobbyDTO

import at.aau.serg.websocketbrokerdemo.messaging.dtos.ExistingPlayerDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NewPlayerJoinedPayloadTest {

    @Test
    fun `NewPlayerJoinedPayload stores values correctly`() {
        val existing = listOf(ExistingPlayerDTO("p2", false, "Scarlett", null))
        val payload = NewPlayerJoinedPayload("p1", listOf("Scarlett", "Mustard"), existing)
        Assertions.assertEquals("p1", payload.playerId)
        Assertions.assertEquals(listOf("Scarlett", "Mustard"), payload.availableCharacters)
        Assertions.assertEquals(existing, payload.existingPlayers)
    }

    @Test
    fun `NewPlayerJoinedPayload equals and hashCode`() {
        val existing = listOf(ExistingPlayerDTO("p2", false, "Scarlett", null))
        val a = NewPlayerJoinedPayload("p1", listOf("Scarlett"), existing)
        val b = NewPlayerJoinedPayload("p1", listOf("Scarlett"), existing)
        Assertions.assertEquals(a, b)
        Assertions.assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `NewPlayerJoinedPayload copy`() {
        val existing = listOf(ExistingPlayerDTO("p2", false, "Scarlett", null))
        val original = NewPlayerJoinedPayload("p1", listOf("Scarlett"), existing)
        val copy = original.copy(playerId = "p99")
        Assertions.assertEquals("p99", copy.playerId)
    }

    @Test
    fun `NewPlayerJoinedPayload toString contains values`() {
        val payload = NewPlayerJoinedPayload("p1", listOf("Scarlett"), emptyList())
        Assertions.assertTrue(payload.toString().contains("p1"))
    }
}