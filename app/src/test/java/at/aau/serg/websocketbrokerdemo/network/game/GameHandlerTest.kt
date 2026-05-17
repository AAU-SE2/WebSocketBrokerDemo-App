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
        GameHandler.onGameFinished = null
        GameHandler.onGameAborted = null
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
    fun `ROLL_DICE with null payload does not crash`() = handle {
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
    fun `MOVE with missing playerId does not crash`() = handle {
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
    fun `END_TURN with missing currentPlayerIndex does not crash`() = handle {
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
    fun `ENTER_ROOM with missing playerId does not crash`() = handle {
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

    @Test
    fun `GAME_FINISHED calls callback`() = handle {
        var winner = ""
        GameHandler.onGameFinished = {
            w -> winner = w
        }
        GameHandler.handle("""{ "type": "GAME_FINISHED", "payload": { "winner": "p1" } }""")
        Assertions.assertEquals("p1", winner)
    }

    @Test
    fun `GAME_FINISHED no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "GAME_FINISHED", "payload": { "winner": "p1" } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `GAME_ABORTED calls callback`() = handle {
        var reason = ""
        GameHandler.onGameAborted = {
            r -> reason = r
        }
        GameHandler.handle("""{ "type": "GAME_ABORTED", "payload": { "reason": "player left" } }""")
        Assertions.assertEquals("player left", reason)
    }

    @Test
    fun `GAME_ABORTED no callback set does not crash`() = handle {
        GameHandler.handle("""{ "type": "GAME_ABORTED", "payload": { "reason": "player left" } }""")
        Assertions.assertTrue(true)
    }

    @Test
    fun `TAKE_HIDDEN_WAY with missing playerId does not crash`() = handle {
        var called = false
        GameHandler.onHiddenWay = {
            _, _ -> called = true
        }
        GameHandler.handle("""{ "type": "TAKE_HIDDEN_WAY", "payload": { } }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `ROLL_DICE with newPosition updates state`() = handle {
        var pos: String? = null
        GameHandler.onRollDice = {
            _, newPosition -> pos = newPosition
        }
        GameHandler.handle("""{ "type": "ROLL_DICE", "payload": { "value": 3, "newPosition": "5,5", "playerId": "p1" } }""")
        Assertions.assertEquals("5,5", pos)
    }

    @Test
    fun `MAKE_ACCUSATION with eliminated true sets isEliminated`() = handle {
        at.aau.serg.websocketbrokerdemo.model.ClientState.playerId = "p1"
        at.aau.serg.websocketbrokerdemo.model.ClientState.isEliminated = false
        GameHandler.handle("""{ "type": "MAKE_ACCUSATION", "payload": { "accuserID": "p1", "suspect": "s", "room": "r", "weapon": "w", "correct": false, "eliminated": true } }""")
        Assertions.assertTrue(at.aau.serg.websocketbrokerdemo.model.ClientState.isEliminated)
    }

    @Test
    fun `SUGGESTION_RESULT with matching cards calls callback`() = handle {
        var cards = listOf<String>()
        GameHandler.onSuggestionResult = {
            _, _, _, _, matchingCards -> cards = matchingCards
        }
        GameHandler.handle("""{ "type": "SUGGESTION_RESULT", "payload": { "suggesterID": "p1", "suspect": "s", "room": "r", "weapon": "w", "matchingCards": [{"name": "knife"}] } }""")
        Assertions.assertEquals(listOf("knife"), cards)
    }
    @Test
    fun `GameHandler can be instantiated`() = handle {
        val handler = GameHandler()
        Assertions.assertNotNull(handler)
    }
    @Test
    fun `ROLL_DICE with currentPhase updates ClientState`() = handle {
        GameHandler.handle("""{ "type": "ROLL_DICE", "payload": { "value": 3, "currentPhase": "WAITING_FOR_ROLL" } }""")
        Assertions.assertEquals("WAITING_FOR_ROLL", at.aau.serg.websocketbrokerdemo.model.ClientState.currentPhase)
    }

    @Test
    fun `SUGGESTION_RESULT with suggesterID matching playerId adds to seenCards`() = handle {
        at.aau.serg.websocketbrokerdemo.model.ClientState.playerId = "p1"
        at.aau.serg.websocketbrokerdemo.model.ClientState.seenCards.clear()
        GameHandler.handle("""{ "type": "SUGGESTION_RESULT", "payload": { "suggesterID": "p1", "suspect": "s", "room": "r", "weapon": "w", "matchingCards": [{"name": "knife"}] } }""")
        Assertions.assertTrue(at.aau.serg.websocketbrokerdemo.model.ClientState.seenCards.contains("knife"))
    }

    @Test
    fun `ROLL_DICE without currentPhase does not update ClientState`() = handle {
        at.aau.serg.websocketbrokerdemo.model.ClientState.currentPhase = "OLD_PHASE"
        GameHandler.handle("""{ "type": "ROLL_DICE", "payload": { "value": 3 } }""")
        Assertions.assertEquals("OLD_PHASE", at.aau.serg.websocketbrokerdemo.model.ClientState.currentPhase)
    }

    @Test
    fun `MOVE with null payload does not crash`() = handle {
        var called = false
        GameHandler.onMove = {
            _, _, _ -> called = true
        }
        GameHandler.handle("""{ "type": "MOVE" }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `END_TURN with null payload does not crash`() = handle {
        var called = false
        GameHandler.onEndTurn = {
            _ -> called = true
        }
        GameHandler.handle("""{ "type": "END_TURN" }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `SUGGESTION_RESULT with null matchingCards array does not crash`() = handle {
        var cards = listOf<String>()
        GameHandler.onSuggestionResult = {
            _, _, _, _, matchingCards -> cards = matchingCards
        }
        GameHandler.handle("""{ "type": "SUGGESTION_RESULT", "payload": { "suggesterID": "p1", "suspect": "s", "room": "r", "weapon": "w" } }""")
        Assertions.assertTrue(cards.isEmpty())
    }
    @Test
    fun `ENTER_ROOM with null payload does not crash`() = handle {
        var called = false
        GameHandler.onEnterRoom = {
            _, _ -> called = true
        }
        GameHandler.handle("""{ "type": "ENTER_ROOM" }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `TAKE_HIDDEN_WAY with null payload does not crash`() = handle {
        var called = false
        GameHandler.onHiddenWay = {
            _, _ -> called = true
        }
        GameHandler.handle("""{ "type": "TAKE_HIDDEN_WAY" }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `MAKE_ACCUSATION with null payload does not crash`() = handle {
        var called = false
        GameHandler.onAccusation = {
            _, _, _, _, _, _ -> called = true
        }
        GameHandler.handle("""{ "type": "MAKE_ACCUSATION" }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `SUGGESTION_RESULT with null payload does not crash`() = handle {
        var called = false
        GameHandler.onSuggestionResult = { _, _, _, _, _ -> called = true }
        GameHandler.handle("""{ "type": "SUGGESTION_RESULT" }""")
        Assertions.assertFalse(called)
    }

    @Test
    fun `GAME_FINISHED with null payload uses empty string`() = handle {
        var winner = "x"
        GameHandler.onGameFinished = { w -> winner = w }
        GameHandler.handle("""{ "type": "GAME_FINISHED" }""")
        Assertions.assertEquals("", winner)
    }

    @Test
    fun `GAME_ABORTED with null payload uses default reason`() = handle {
        var reason = ""
        GameHandler.onGameAborted = {
            r -> reason = r
        }
        GameHandler.handle("""{ "type": "GAME_ABORTED" }""")
        Assertions.assertEquals("Game aborted", reason)
    }
}