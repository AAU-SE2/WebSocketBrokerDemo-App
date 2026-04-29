package at.aau.serg.websocketbrokerdemo.network.game

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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

    @Test
    fun `ROLL_DICE calls callback`() {
        var result = 0
        GameHandler.onRollDice = { result = it }

        GameHandler.handle("""{ "type": "ROLL_DICE", "payload": { "value": 5 } }""")

        Assertions.assertEquals(5, result)
    }

    @Test
    fun `ROLL_DICE returns when payload missing`() {
        var called = false
        GameHandler.onRollDice = { called = true }

        GameHandler.handle("""{ "type": "ROLL_DICE" }""")

        Assertions.assertFalse(called)
    }

    @Test
    fun `MOVE calls callback`() {
        var p = ""
        var pos = ""
        GameHandler.onMove = { player, position -> p = player; pos = position }

        GameHandler.handle("""{ "type": "MOVE", "payload": { "playerId": "p1", "position": "A1" } }""")

        Assertions.assertEquals("p1", p)
        Assertions.assertEquals("A1", pos)
    }

    @Test
    fun `MOVE returns when playerId missing`() {
        var called = false
        GameHandler.onMove = { _, _ -> called = true }

        GameHandler.handle("""{ "type": "MOVE", "payload": { } }""")

        Assertions.assertFalse(called)
    }

    @Test
    fun `END_TURN calls callback`() {
        var prev = -1
        var next = -1
        GameHandler.onEndTurn = { p, n -> prev = p; next = n }

        GameHandler.handle("""{ "type": "END_TURN", "payload": { "previousPlayerIndex": 1, "nextPlayerIndex": 2 } }""")

        Assertions.assertEquals(1, prev)
        Assertions.assertEquals(2, next)
    }

    @Test
    fun `END_TURN returns when previous missing`() {
        var called = false
        GameHandler.onEndTurn = { _, _ -> called = true }

        GameHandler.handle("""{ "type": "END_TURN", "payload": { "nextPlayerIndex": 2 } }""")

        Assertions.assertFalse(called)
    }

    @Test
    fun `ENTER_ROOM calls callback`() {
        var room = ""
        GameHandler.onEnterRoom = { room = it }

        GameHandler.handle("""{ "type": "ENTER_ROOM", "payload": { "roomId": "room1" } }""")

        Assertions.assertEquals("room1", room)
    }

    @Test
    fun `ENTER_ROOM returns when missing`() {
        var called = false
        GameHandler.onEnterRoom = { called = true }

        GameHandler.handle("""{ "type": "ENTER_ROOM", "payload": { } }""")

        Assertions.assertFalse(called)
    }

    @Test
    fun `TAKE_HIDDEN_WAY calls callback`() {
        var called = false
        GameHandler.onHiddenWay = { called = true }

        GameHandler.handle("""{ "type": "TAKE_HIDDEN_WAY" }""")

        Assertions.assertTrue(called)
    }

    @Test
    fun `MAKE_ACCUSATION calls callback`() {
        var result = ""
        GameHandler.onAccusation = { result = it }

        GameHandler.handle("""{ "type": "MAKE_ACCUSATION", "payload": { "x": 1 } }""")

        Assertions.assertTrue(result.contains("x"))
    }

    @Test
    fun `MAKE_ACCUSATION returns when payload missing`() {
        var called = false
        GameHandler.onAccusation = { called = true }

        GameHandler.handle("""{ "type": "MAKE_ACCUSATION" }""")

        Assertions.assertFalse(called)
    }

    @Test
    fun `MAKE_SUGGESTION calls callback`() {
        var result = ""
        GameHandler.onSuggestion = { result = it }

        GameHandler.handle("""{ "type": "MAKE_SUGGESTION", "payload": { "y": 2 } }""")

        Assertions.assertTrue(result.contains("y"))
    }

    @Test
    fun `MAKE_SUGGESTION returns when payload missing`() {
        var called = false
        GameHandler.onSuggestion = { called = true }

        GameHandler.handle("""{ "type": "MAKE_SUGGESTION" }""")

        Assertions.assertFalse(called)
    }

    @Test
    fun `invalid JSON does not crash`() {
        GameHandler.handle("not a json")

        Assertions.assertTrue(true)
    }

    // Callbacks nicht gesetzt (?.invoke null branch)
    @Test
    fun `ROLL_DICE no callback set does not crash`() {
        GameHandler.onRollDice = null
        GameHandler.handle("""{ "type": "ROLL_DICE", "payload": { "value": 5 } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `MOVE no callback set does not crash`() {
        GameHandler.onMove = null
        GameHandler.handle("""{ "type": "MOVE", "payload": { "playerId": "p1", "position": "A1" } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `END_TURN no callback set does not crash`() {
        GameHandler.onEndTurn = null
        GameHandler.handle("""{ "type": "END_TURN", "payload": { "previousPlayerIndex": 1, "nextPlayerIndex": 2 } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `ENTER_ROOM no callback set does not crash`() {
        GameHandler.onEnterRoom = null
        GameHandler.handle("""{ "type": "ENTER_ROOM", "payload": { "roomId": "room1" } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `TAKE_HIDDEN_WAY no callback set does not crash`() {
        GameHandler.onHiddenWay = null
        GameHandler.handle("""{ "type": "TAKE_HIDDEN_WAY" }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `MAKE_ACCUSATION no callback set does not crash`() {
        GameHandler.onAccusation = null
        GameHandler.handle("""{ "type": "MAKE_ACCUSATION", "payload": { "x": 1 } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `MAKE_SUGGESTION no callback set does not crash`() {
        GameHandler.onSuggestion = null
        GameHandler.handle("""{ "type": "MAKE_SUGGESTION", "payload": { "y": 2 } }""")
        Assertions.assertTrue(true)
    }

    // catch-Block testen
    @Test
    fun `unknown type goes to catch block`() {
        GameHandler.handle("""{ "type": "UNKNOWN_TYPE" }""")
        Assertions.assertTrue(true)
    }
}