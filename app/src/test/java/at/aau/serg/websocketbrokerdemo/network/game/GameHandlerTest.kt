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
        GameHandler.onSuggestionResult = null
    }

    private fun handle(block: () -> Unit) {
        Mockito.mockStatic(Log::class.java).use { block() }
    }

    @Test
    fun `ROLL_DICE calls callback`() = handle {
        var result = 0
        GameHandler.onRollDice = {
            value, _ -> result = value
        }
        GameHandler.handle("""{ "type": "ROLL_DICE", "payload": { "value": 5 } }""")
        Assertions.assertEquals(5, result)
    }

    @Test
    fun `ROLL_DICE returns when payload missing`() = handle {
        var called = false
        GameHandler.onRollDice = {
            _, _ -> called = true
        }
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
        GameHandler.onMove = {
            player, position, _ -> p = player; pos = position
        }
        GameHandler.handle("""{ "type": "MOVE", "payload": { "playerId": "p1", "position": "A1", "movesLeft": 2 } }""")
        Assertions.assertEquals("p1", p)
        Assertions.assertEquals("A1", pos)
    }

    @Test
    fun `MOVE returns when playerId missing`() = handle {
        var called = false
        GameHandler.onMove = {
            _, _, _ -> called = true
        }
        GameHandler.handle("""{ "type": "MOVE", "payload": { } }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `MOVE no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "MOVE", "payload": { "playerId": "p1", "position": "A1", "movesLeft": 2 } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `END_TURN calls callback`() = handle {
        var currentPlayerIndex = -1
        GameHandler.onEndTurn = {
            index -> currentPlayerIndex = index
        }
        GameHandler.handle("""{ "type": "END_TURN", "payload": { "currentPlayerIndex": 2 } }""")
        Assertions.assertEquals(2, currentPlayerIndex)
    }

    @Test
    fun `END_TURN returns when previous missing`() = handle {
        var called = false
        GameHandler.onEndTurn = {
            called = true
        }
        GameHandler.handle("""{ "type": "END_TURN", "payload": { } }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `END_TURN no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "END_TURN", "payload": { "currentPlayerIndex": 2 } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `ENTER_ROOM calls callback`() = handle {
        var room = ""
        GameHandler.onEnterRoom = {
            _, roomId -> room = roomId
        }
        GameHandler.handle("""{ "type": "ENTER_ROOM", "payload": { "playerId": "p1", "roomId": "room1" } }""")
        Assertions.assertEquals("room1", room)
    }

    @Test
    fun `ENTER_ROOM returns when missing`() = handle {
        var called = false
        GameHandler.onEnterRoom = {
            _, _ -> called = true
        }
        GameHandler.handle("""{ "type": "ENTER_ROOM", "payload": { } }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `ENTER_ROOM no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "ENTER_ROOM", "payload": { "playerId": "p1", "roomId": "room1" } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `TAKE_HIDDEN_WAY calls callback`() = handle {
        var called = false
        GameHandler.onHiddenWay = {
            _, _ -> called = true
        }
        GameHandler.handle("""{ "type": "TAKE_HIDDEN_WAY", "payload": { "playerId": "p1", "targetRoom": "room1" } }""")
        Assertions.assertTrue(called)
    }

    @Test
    fun `TAKE_HIDDEN_WAY no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "TAKE_HIDDEN_WAY", "payload": { "playerId": "p1", "targetRoom": "room1" } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `MAKE_ACCUSATION calls callback`() = handle {
        var called = false
        GameHandler.onAccusation = {
            _, _, _, _, _, _ -> called = true
        }
        GameHandler.handle("""{ "type": "MAKE_ACCUSATION", "payload": { "accuserID": "p1", "suspect": "s", "room": "r", "weapon": "w", "correct": true } }""")
        Assertions.assertTrue(called)
    }

    @Test
    fun `MAKE_ACCUSATION returns when payload missing`() = handle {
        var called = false
        GameHandler.onAccusation = {
            _, _, _, _, _, _ -> called = true
        }
        GameHandler.handle("""{ "type": "MAKE_ACCUSATION" }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `MAKE_ACCUSATION no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "MAKE_ACCUSATION", "payload": { "accuserID": "p1", "suspect": "s", "room": "r", "weapon": "w", "correct": true } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `SUGGESTION_RESULT calls callback`() = handle {
        var called = false
        GameHandler.onSuggestionResult = {
            _, _, _, _, _ -> called = true
        }
        GameHandler.handle("""{ "type": "SUGGESTION_RESULT", "payload": { "suggesterID": "p1", "suspect": "s", "room": "r", "weapon": "w", "matchingCards": [] } }""")
        Assertions.assertTrue(called)
    }

    @Test
    fun `SUGGESTION_RESULT returns when payload missing`() = handle {
        var called = false
        GameHandler.onSuggestionResult = {
            _, _, _, _, _ -> called = true
        }
        GameHandler.handle("""{ "type": "SUGGESTION_RESULT" }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `SUGGESTION_RESULT no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "SUGGESTION_RESULT", "payload": { "suggesterID": "p1", "suspect": "s", "room": "r", "weapon": "w", "matchingCards": [] } }""")
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