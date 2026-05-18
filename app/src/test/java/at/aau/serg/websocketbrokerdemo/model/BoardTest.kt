package at.aau.serg.websocketbrokerdemo.model

import com.example.myapplication.R
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BoardTest {

    @Test
    fun `players list is empty on init`() {
        val board = Board()
        assertTrue(board.players.isEmpty())
    }

    @Test
    fun `cards list is empty on init`() {
        val board = Board()
        assertTrue(board.cards.isEmpty())
    }

    @Test
    fun `players can be added`() {
        val board = Board()
        board.players.add(Player(1, "Alice"))
        assertEquals(1, board.players.size)
    }

    @Test
    fun `cards can be added`() {
        val board = Board()
        board.cards.add(Card("c1", R.drawable.cmrslavender))
        assertEquals(1, board.cards.size)
    }
}