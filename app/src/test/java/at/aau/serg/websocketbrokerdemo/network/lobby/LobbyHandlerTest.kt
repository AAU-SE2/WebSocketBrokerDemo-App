package at.aau.serg.websocketbrokerdemo.network.lobby

import at.aau.serg.websocketbrokerdemo.model.ClientState
import at.aau.serg.websocketbrokerdemo.messaging.dtos.lobbyDTO.GameFullPayload
import at.aau.serg.websocketbrokerdemo.messaging.dtos.lobbyDTO.NewPlayerJoinedPayload
import at.aau.serg.websocketbrokerdemo.messaging.dtos.lobbyDTO.PlayerRejoinedPayload
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LobbyHandlerTest {

    @BeforeEach
    fun setup() {
        LobbyHandler.onNewPlayerJoined = null
        LobbyHandler.onPlayerRejoined = null
        LobbyHandler.onGameFull = null
        LobbyHandler.onLobbyJoined = null
        LobbyHandler.onPlayerRemoved = null
        ClientState.players = emptyList()
        ClientState.availableCharacters = emptyList()
        ClientState.playerId = ""
        ClientState.myCards = emptyList()
        ClientState.seenCards.clear()
        ClientState.playerPositions.clear()
        ClientState.playerCharacterMap.clear()
        ClientState.eliminatedPlayers.clear()
        ClientState.currentPhase = ""
        ClientState.currentPlayerIndex = 0
        ClientState.remainingMoves = 0
        ClientState.myCharacter = null
        ClientState.isEliminated = false
        LobbyHandler.onOtherPlayerRemoved = null
        LobbyHandler.onSetReady = null
        LobbyHandler.onGameStarted = null
        LobbyHandler.onStartGameError = null
        LobbyHandler.onPlayerRejoinedRunning = null
    }

    private fun buildNewPlayerJoined(
        playerId: String = "p1",
        characters: List<String> = listOf("Scarlett", "Mustard"),
        existingPlayers: String = """[{"playerId":"p2","ready":false,"character":"","position":""}]"""
    ) = """
        {
          "type": "NEW_PLAYER_JOINED",
          "payload": {
            "playerId": "$playerId",
            "availableCharacters": ${characters.joinToString(",", "[", "]") { "\"$it\"" }},
            "existingPlayers": $existingPlayers
          }
        }
    """.trimIndent()

    private fun buildPlayerRejoined(
        playerId: String = "p1",
        existingPlayers: String = """[{"playerId":"p2","ready":false,"character":"","position":""}]"""
    ) = """
        {
          "type": "PLAYER_REJOINED",
          "payload": {
            "playerId": "$playerId",
            "existingPlayers": $existingPlayers
          }
        }
    """.trimIndent()

    private fun buildGameFull(playerId: String = "p1", message: String = "full") = """
        {
          "type": "GAME_FULL",
          "payload": {
            "playerId": "$playerId",
            "message": "$message"
          }
        }
    """.trimIndent()

    private fun buildPlayerRemoved(playerId: String = "p1") = """
        {
          "type": "PLAYER_REMOVED",
          "payload": {
            "playerId": "$playerId"
          }
        }
    """.trimIndent()


    @Test
    fun `NEW_PLAYER_JOINED calls onNewPlayerJoined with correct data`() {
        var result: NewPlayerJoinedPayload? = null
        LobbyHandler.onNewPlayerJoined = { result = it }

        LobbyHandler.handle(buildNewPlayerJoined())

        Assertions.assertEquals("p1", result?.playerId)
        Assertions.assertEquals(listOf("Scarlett", "Mustard"), result?.availableCharacters)
    }

    @Test
    fun `NEW_PLAYER_JOINED updates ClientState`() {
        LobbyHandler.handle(buildNewPlayerJoined())

        Assertions.assertEquals(listOf("Scarlett", "Mustard"), ClientState.availableCharacters)
        Assertions.assertEquals(1, ClientState.players.size)
        Assertions.assertEquals("p2", ClientState.players[0].playerId)
    }

    @Test
    fun `NEW_PLAYER_JOINED calls onLobbyJoined`() {
        ClientState.playerId = "p1"

        var called = false
        LobbyHandler.onLobbyJoined = { called = true }

        LobbyHandler.handle(buildNewPlayerJoined())

        Assertions.assertTrue(called)
    }

    @Test
    fun `NEW_PLAYER_JOINED no callbacks set does not crash`() {
        LobbyHandler.handle(buildNewPlayerJoined())
        Assertions.assertTrue(true)
    }

    @Test
    fun `NEW_PLAYER_JOINED parses player with character and position`() {
        var result: NewPlayerJoinedPayload? = null
        LobbyHandler.onNewPlayerJoined = { result = it }

        val msg = buildNewPlayerJoined(
            existingPlayers = """[{"playerId":"p2","ready":true,"character":"Scarlett","position":"A1"}]"""
        )
        LobbyHandler.handle(msg)

        Assertions.assertEquals("Scarlett", result?.existingPlayers?.get(0)?.character)
        Assertions.assertEquals("A1", result?.existingPlayers?.get(0)?.position)
    }


    @Test
    fun `PLAYER_REJOINED calls onPlayerRejoined with correct data`() {
        var result: PlayerRejoinedPayload? = null
        LobbyHandler.onPlayerRejoined = { result = it }

        LobbyHandler.handle(buildPlayerRejoined())

        Assertions.assertEquals("p1", result?.playerId)
        Assertions.assertEquals(1, result?.existingPlayers?.size)
    }

    @Test
    fun `PLAYER_REJOINED updates ClientState players`() {
        LobbyHandler.handle(buildPlayerRejoined())

        Assertions.assertEquals(1, ClientState.players.size)
        Assertions.assertEquals("p2", ClientState.players[0].playerId)
    }

    @Test
    fun `PLAYER_REJOINED calls onLobbyJoined`() {
        ClientState.playerId = "p1"

        var called = false
        LobbyHandler.onPlayerRejoined = { called = true }

        LobbyHandler.handle(buildPlayerRejoined())

        Assertions.assertTrue(called)
    }

    @Test
    fun `PLAYER_REJOINED no callbacks set does not crash`() {
        LobbyHandler.handle(buildPlayerRejoined())
        Assertions.assertTrue(true)
    }


    @Test
    fun `GAME_FULL calls onGameFull with correct data`() {
        var result: GameFullPayload? = null
        LobbyHandler.onGameFull = { result = it }

        LobbyHandler.handle(buildGameFull(playerId = "p1", message = "Game is full"))

        Assertions.assertEquals("p1", result?.playerId)
        Assertions.assertEquals("Game is full", result?.message)
    }

    @Test
    fun `GAME_FULL no callback set does not crash`() {
        LobbyHandler.handle(buildGameFull())
        Assertions.assertTrue(true)
    }


    @Test
    fun `PLAYER_REMOVED calls onPlayerRemoved when playerId matches ClientState`() {
        ClientState.playerId = "p1"
        var removedId = ""
        LobbyHandler.onPlayerRemoved = { removedId = it }

        LobbyHandler.handle(buildPlayerRemoved("p1"))

        Assertions.assertEquals("p1", removedId)
    }

    @Test
    fun `PLAYER_REMOVED does not call onPlayerRemoved when playerId does not match`() {
        ClientState.playerId = "p99"
        var called = false
        LobbyHandler.onPlayerRemoved = { called = true }

        LobbyHandler.handle(buildPlayerRemoved("p1"))

        Assertions.assertFalse(called)
    }

    @Test
    fun `PLAYER_REMOVED no callback set does not crash`() {
        ClientState.playerId = "p1"
        LobbyHandler.handle(buildPlayerRemoved("p1"))
        Assertions.assertTrue(true)
    }

    @Test
    fun `PLAYER_REMOVED calls other player callback`() {
        ClientState.playerId = "p1"
        var removedPlayer = ""

        LobbyHandler.onOtherPlayerRemoved = { removedPlayer = it }

        LobbyHandler.handle(buildPlayerRemoved("p2"))

        Assertions.assertEquals("p2", removedPlayer)
    }

    @Test
    fun `START_GAME_ERROR calls callback`() {
        var error = ""

        LobbyHandler.onStartGameError = { error = it }

        val msg = """
            {
          "type": "START_GAME_ERROR",
          "payload": {
            "reason": "Not ready"
          }
        }
    """.trimIndent()

        LobbyHandler.handle(msg)

        Assertions.assertEquals("Not ready", error)
    }

    @Test
    fun `SET_READY updates ClientState`() {
        val msg = """
        {
          "type": "SET_CHARACTER_TYPE_AND_STATUS_READY",
          "payload": {
            "playerId": "p1",
            "characterType": "DR_RED",
            "ready": true,
            "availableCharacters": ["DR_BLUE"],
            "existingPlayers": [
              {
                "playerId": "p1",
                "ready": true,
                "characterType": "DR_RED",
                "position": ""
              }
            ]
          }
        }
    """.trimIndent()

        LobbyHandler.handle(msg)

        Assertions.assertEquals(listOf("DR_BLUE"), ClientState.availableCharacters)
        Assertions.assertEquals(1, ClientState.players.size)
        Assertions.assertEquals("DR_RED", ClientState.players[0].character)
        Assertions.assertTrue(ClientState.players[0].ready)
    }

    @Test
    fun `GAME_STARTED updates state`() {
        ClientState.playerId = "p1"

        ClientState.players = listOf(
            at.aau.serg.websocketbrokerdemo.messaging.dtos.ExistingPlayerDTO(
                "p1",
                true,
                "DR_RED",
                null
            )
        )

        var started = false
        LobbyHandler.onGameStarted = { started = true }

        val msg = """
    {
      "type": "GAME_STARTED",
      "payload": {
        "currentPhase": "WAITING_FOR_ROLL",
        "currentPlayerIndex": 0,
        "players": [
          {
            "playerId": "p1",
            "cards": [
              { "name": "KNIFE" }
            ]
          }
        ]
      }
    }
    """.trimIndent()

        LobbyHandler.handle(msg)

        Assertions.assertTrue(started)
        Assertions.assertEquals("WAITING_FOR_ROLL", ClientState.currentPhase)
        Assertions.assertEquals(listOf("KNIFE"), ClientState.myCards)
        Assertions.assertTrue(ClientState.seenCards.contains("KNIFE"))
        Assertions.assertEquals("DR_RED", ClientState.playerCharacterMap["p1"])
    }

    @Test
    fun `PLAYER_REJOINED_RUNNING updates state`() {
        var called = false
        LobbyHandler.onPlayerRejoinedRunning = { called = true }

        val msg = """
    {
      "type": "PLAYER_REJOINED_RUNNING",
      "payload": {
        "playerId": "p1",
        "myCharacter": "DR_RED",
        "myCards": [
          { "name": "KNIFE" }
        ],
        "isEliminated": true,
        "players": [
          {
            "playerId": "p1",
            "ready": true,
            "characterType": "DR_RED",
            "position": "1,1"
          }
        ],
        "playerPositions": {
          "p1": "1,1"
        },
        "playerCharacterMap": {
          "p1": "DR_RED"
        },
        "eliminatedPlayers": ["p1"],
        "currentPlayerId": "p1",
        "currentPlayerIndex": 0,
        "currentPhase": "IN_ROOM",
        "remainingMoves": 2
      }
    }
    """.trimIndent()

        LobbyHandler.handle(msg)

        Assertions.assertTrue(called)
        Assertions.assertEquals("p1", ClientState.playerId)
        Assertions.assertEquals("DR_RED", ClientState.myCharacter)
        Assertions.assertEquals(listOf("KNIFE"), ClientState.myCards)
        Assertions.assertTrue(ClientState.seenCards.contains("KNIFE"))
        Assertions.assertEquals("1,1", ClientState.playerPositions["p1"])
        Assertions.assertEquals("DR_RED", ClientState.playerCharacterMap["p1"])
        Assertions.assertTrue(ClientState.eliminatedPlayers.contains("p1"))
        Assertions.assertTrue(ClientState.isEliminated)
    }

    @Test
    fun `PLAYER_REJOINED updates availableCharacters`() {

        val msg = """
    {
      "type": "PLAYER_REJOINED",
      "payload": {
        "playerId": "p1",
        "availableCharacters": ["DR_RED"],
        "existingPlayers": []
      }
    }
    """.trimIndent()

        LobbyHandler.handle(msg)

        Assertions.assertEquals(listOf("DR_RED"), ClientState.availableCharacters)
    }
}