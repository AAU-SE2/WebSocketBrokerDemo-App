package at.aau.serg.websocketbrokerdemo.network.game

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


import android.util.Log
import org.mockito.Mockito

class GameHandlerTest {

    @BeforeEach
    fun setup() {
        GameHandler.onRollDice = null
        GameHandler.onMove = null
        GameHandler.onEndTurn = null
        GameHandler.onEnterRoom = null
        GameHandler.onHiddenWay = null
        GameHandler.onAccusation = null
        GameHandler.onSuggestion = null
    }

    private fun handle(block: () -> Unit) {
        Mockito.mockStatic(Log::class.java).use { block() }
    }

    @Test
    fun `ROLL_DICE calls callback`() = handle {
        var result = 0
        GameHandler.onRollDice = { result = it }
        GameHandler.handle("""{ "type": "ROLL_DICE", "payload": { "value": 5 } }""")
        Assertions.assertEquals(5, result)
    }

    @Test
    fun `ROLL_DICE returns when payload missing`() = handle {
        var called = false
        GameHandler.onRollDice = { called = true }
        GameHandler.handle("""{ "type": "ROLL_DICE" }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `ROLL_DICE no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "ROLL_DICE", "payload": { "value": 5 } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `MOVE calls callback`() = handle {
        var p = ""
        var pos = ""
        GameHandler.onMove = { player, position -> p = player; pos = position }
        GameHandler.handle("""{ "type": "MOVE", "payload": { "playerId": "p1", "position": "A1" } }""")
        Assertions.assertEquals("p1", p)
        Assertions.assertEquals("A1", pos)
    }

    @Test
    fun `MOVE returns when playerId missing`() = handle {
        var called = false
        GameHandler.onMove = { _, _ -> called = true }
        GameHandler.handle("""{ "type": "MOVE", "payload": { } }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `MOVE no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "MOVE", "payload": { "playerId": "p1", "position": "A1" } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `END_TURN calls callback`() = handle {
        var prev = -1
        var next = -1
        GameHandler.onEndTurn = { p, n -> prev = p; next = n }
        GameHandler.handle("""{ "type": "END_TURN", "payload": { "previousPlayerIndex": 1, "nextPlayerIndex": 2 } }""")
        Assertions.assertEquals(1, prev)
        Assertions.assertEquals(2, next)
    }

    @Test
    fun `END_TURN returns when previous missing`() = handle {
        var called = false
        GameHandler.onEndTurn = { _, _ -> called = true }
        GameHandler.handle("""{ "type": "END_TURN", "payload": { "nextPlayerIndex": 2 } }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `END_TURN no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "END_TURN", "payload": { "previousPlayerIndex": 1, "nextPlayerIndex": 2 } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `ENTER_ROOM calls callback`() = handle {
        var room = ""
        GameHandler.onEnterRoom = { room = it }
        GameHandler.handle("""{ "type": "ENTER_ROOM", "payload": { "roomId": "room1" } }""")
        Assertions.assertEquals("room1", room)
    }

    @Test
    fun `ENTER_ROOM returns when missing`() = handle {
        var called = false
        GameHandler.onEnterRoom = { called = true }
        GameHandler.handle("""{ "type": "ENTER_ROOM", "payload": { } }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `ENTER_ROOM no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "ENTER_ROOM", "payload": { "roomId": "room1" } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `TAKE_HIDDEN_WAY calls callback`() = handle {
        var called = false
        GameHandler.onHiddenWay = { called = true }
        GameHandler.handle("""{ "type": "TAKE_HIDDEN_WAY" }""")
        Assertions.assertTrue(called)
    }

    @Test
    fun `TAKE_HIDDEN_WAY no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "TAKE_HIDDEN_WAY" }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `MAKE_ACCUSATION calls callback`() = handle {
        var result = ""
        GameHandler.onAccusation = { result = it }
        GameHandler.handle("""{ "type": "MAKE_ACCUSATION", "payload": { "x": 1 } }""")
        Assertions.assertTrue(result.contains("x"))
    }

    @Test
    fun `MAKE_ACCUSATION returns when payload missing`() = handle {
        var called = false
        GameHandler.onAccusation = { called = true }
        GameHandler.handle("""{ "type": "MAKE_ACCUSATION" }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `MAKE_ACCUSATION no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "MAKE_ACCUSATION", "payload": { "x": 1 } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `MAKE_SUGGESTION calls callback`() = handle {
        var result = ""
        GameHandler.onSuggestion = { result = it }
        GameHandler.handle("""{ "type": "MAKE_SUGGESTION", "payload": { "y": 2 } }""")
        Assertions.assertTrue(result.contains("y"))
    }

    @Test
    fun `MAKE_SUGGESTION returns when payload missing`() = handle {
        var called = false
        GameHandler.onSuggestion = { called = true }
        GameHandler.handle("""{ "type": "MAKE_SUGGESTION" }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `MAKE_SUGGESTION no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "MAKE_SUGGESTION", "payload": { "y": 2 } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `invalid JSON does not crash`() = handle {
        GameHandler.handle("not a json")
        Assertions.assertTrue(true)
    }

    @Test
    fun `unknown type goes to catch block`() = handle {
        GameHandler.handle("""{ "type": "UNKNOWN_TYPE" }""")
        Assertions.assertTrue(true)
    }
}