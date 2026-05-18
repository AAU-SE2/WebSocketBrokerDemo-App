package at.aau.serg.websocketbrokerdemo.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PlayerTest {

    @Test
    fun `player is not ready by default`() {
        val player = Player(1, "Alice")
        assertFalse(player.ready)
    }

    @Test
    fun `player ready can be set to true`() {
        val player = Player(1, "Alice")
        player.ready = true
        assertTrue(player.ready)
    }

    @Test
    fun `player id and name are set correctly`() {
        val player = Player(42, "Bob")
        assertEquals(42, player.id)
        assertEquals("Bob", player.name)
    }
}