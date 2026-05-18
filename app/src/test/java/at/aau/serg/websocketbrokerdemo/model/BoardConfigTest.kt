package at.aau.serg.websocketbrokerdemo.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BoardConfigTest {
    @Test
    fun `COLS is 13`() {
        assertEquals(13, BoardConfig.COLS)
    }

    @Test
    fun `ROWS is 9`() {
        assertEquals(9, BoardConfig.ROWS)
    }

    @Test
    fun `isWalkable returns true for valid cell`() {
        assertTrue(BoardConfig.isWalkable(0, 0))
        assertTrue(BoardConfig.isWalkable(12, 8))
    }

    @Test
    fun `isWalkable returns false for out of bounds`() {
        assertFalse(BoardConfig.isWalkable(-1, 0))
        assertFalse(BoardConfig.isWalkable(13, 0))
        assertFalse(BoardConfig.isWalkable(0, 9))
    }

    @Test
    fun `isAdjacent returns true for horizontally adjacent cells`() {
        assertTrue(BoardConfig.isAdjacent(0, 0, 1, 0))
    }

    @Test
    fun `isAdjacent returns true for vertically adjacent cells`() {
        assertTrue(BoardConfig.isAdjacent(0, 0, 0, 1))
    }

    @Test
    fun `isAdjacent returns false for diagonal cells`() {
        assertFalse(BoardConfig.isAdjacent(0, 0, 1, 1))
    }

    @Test
    fun `isAdjacent returns false for same cell`() {
        assertFalse(BoardConfig.isAdjacent(0, 0, 0, 0))
    }

    @Test
    fun `getRoomAtDoor returns correct room`() {
        assertEquals("KITCHEN", BoardConfig.getRoomAtDoor(0, 0))
        assertEquals("BALLROOM", BoardConfig.getRoomAtDoor(12, 0))
    }

    @Test
    fun `getRoomAtDoor returns null for non-door cell`() {
        assertNull(BoardConfig.getRoomAtDoor(5, 5))
    }

    @Test
    fun `HIDDEN_PASSAGES are symmetric`() {
        for ((from, to) in BoardConfig.HIDDEN_PASSAGES) {
            assertEquals(from, BoardConfig.HIDDEN_PASSAGES[to])
        }
    }

    @Test
    fun `ALL_CHARACTERS contains 4 entries`() {
        assertEquals(4, BoardConfig.ALL_CHARACTERS.size)
    }

    @Test
    fun `ALL_WEAPONS contains 5 entries`() {
        assertEquals(5, BoardConfig.ALL_WEAPONS.size)
    }

    @Test
    fun `ALL_ROOMS contains 6 entries`() {
        assertEquals(6, BoardConfig.ALL_ROOMS.size)
    }

    @Test
    fun `CHARACTER_START_POSITIONS contains all characters`() {
        for (character in BoardConfig.ALL_CHARACTERS) {
            assertTrue(BoardConfig.CHARACTER_START_POSITIONS.containsKey(character))
        }
    }
}