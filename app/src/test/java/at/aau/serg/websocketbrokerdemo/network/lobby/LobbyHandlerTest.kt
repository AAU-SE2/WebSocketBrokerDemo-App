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

    // ---------------------------
    // NEW_PLAYER_JOINED
    // ---------------------------

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

    // ---------------------------
    // PLAYER_REJOINED
    // ---------------------------

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
        var called = false
        LobbyHandler.onLobbyJoined = { called = true }

        LobbyHandler.handle(buildPlayerRejoined())

        Assertions.assertTrue(called)
    }

    @Test
    fun `PLAYER_REJOINED no callbacks set does not crash`() {
        LobbyHandler.handle(buildPlayerRejoined())
        Assertions.assertTrue(true)
    }

    // ---------------------------
    // GAME_FULL
    // ---------------------------

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

    // ---------------------------
    // PLAYER_REMOVED
    // ---------------------------

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
}