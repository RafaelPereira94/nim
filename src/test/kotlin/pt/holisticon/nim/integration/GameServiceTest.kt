package pt.holisticon.nim.integration

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
import org.springframework.context.annotation.Import
import pt.holisticon.nim.TestNimApplication
import pt.holisticon.nim.domain.entity.GameState
import pt.holisticon.nim.domain.enums.PlayerType
import pt.holisticon.nim.exception.EntityNotFoundException
import pt.holisticon.nim.exception.InvalidMoveException
import pt.holisticon.nim.exception.InvalidParametersException
import pt.holisticon.nim.repository.GameRepository
import pt.holisticon.nim.service.GameService
import pt.holisticon.nim.util.Constant.GAME_RESET
import pt.holisticon.nim.util.Constant.MOVE_SUCCESSFUL
import pt.holisticon.nim.util.RandomNumberGenerator

@DataRedisTest
@Import(TestNimApplication::class, GameService::class, RandomNumberGenerator::class)
class GameServiceTest {

    @Autowired
    private lateinit var gameRepository: GameRepository
    private lateinit var randomNumberGenerator: RandomNumberGenerator
    private lateinit var gameService: GameService

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        randomNumberGenerator = mock(RandomNumberGenerator::class.java)
        gameService = GameService(gameRepository, randomNumberGenerator)
    }

    @Test
    fun testStartGame() {
        val totalMatches = 13
        val maxMatchesPerTurn = 3
        val actualGameState = gameService.startGame(totalMatches, maxMatchesPerTurn)

        val expectedGameState = gameService.getGameState(actualGameState.gameId!!)

        assertEquals(expectedGameState.gameId, actualGameState.gameId)
        assertEquals(expectedGameState.totalMatches, actualGameState.totalMatches)
        assertEquals(expectedGameState.maxMatchesPerTurn, actualGameState.maxMatchesPerTurn)
        assertEquals(expectedGameState.matchesInHeap, actualGameState.matchesInHeap)
        assertEquals(expectedGameState.isGameOver, actualGameState.isGameOver)
        assertEquals(expectedGameState.currentPlayer, actualGameState.currentPlayer)
    }

    @Test
    fun testInvalidTotalMatchesStartGame() {
        assertThrows<InvalidParametersException> {
            val totalMatches = 0
            val maxMatchesPerTurn = 3
            gameService.startGame(totalMatches, maxMatchesPerTurn)
        }
    }

    @Test
    fun testInvalidMaxMatchesPerTurnStartGame() {
        assertThrows<InvalidParametersException> {
            val totalMatches = 13
            val maxMatchesPerTurn = -1
            gameService.startGame(totalMatches, maxMatchesPerTurn)
        }
    }

    @Test
    fun testGetGameState() {
        val actualGameState = gameService.startGame(13, 3)
        val expectedGameState = gameService.getGameState(actualGameState.gameId!!)

        assertNotNull(expectedGameState)
        assertEquals(expectedGameState.gameId, actualGameState.gameId)
        assertEquals(expectedGameState.totalMatches, actualGameState.totalMatches)
        assertEquals(expectedGameState.maxMatchesPerTurn, actualGameState.maxMatchesPerTurn)
        assertEquals(expectedGameState.matchesInHeap, actualGameState.matchesInHeap)
        assertEquals(expectedGameState.isGameOver, actualGameState.isGameOver)
        assertEquals(expectedGameState.currentPlayer, actualGameState.currentPlayer)
    }

    @Test
    fun testGetInvalidGameState() {
        assertThrows<EntityNotFoundException> {
            gameService.getGameState("abc")
        }
    }

    @Test
    fun testResetNonExistentGame() {
        assertThrows<EntityNotFoundException> {
            gameService.resetGame("foo")
        }
    }

    @Test
    fun testResetExistentGame() {
        val gameState = gameService.startGame(20, 5)

        val actualGameState = gameService.resetGame(gameState.gameId!!)

        assertNotNull(actualGameState)
        assertEquals(gameState.gameId, actualGameState.gameId)
        assertEquals(13, actualGameState.totalMatches)
        assertEquals(3, actualGameState.maxMatchesPerTurn)
        assertEquals(13, actualGameState.matchesInHeap)
        assertEquals(false, actualGameState.isGameOver)
        assertEquals(PlayerType.PLAYER, actualGameState.currentPlayer)
        assertEquals(GAME_RESET, actualGameState.message)
    }

    @Test
    fun testValidPlayerMove() {
        val startGameState = gameService.startGame(13, 3)
        val playerMoveState = gameService.playerMove(startGameState.gameId!!, 2)

        assertNotNull(playerMoveState)
        assertEquals(startGameState.gameId, playerMoveState.gameId)
        assertEquals(13, playerMoveState.totalMatches)
        assertEquals(3, playerMoveState.maxMatchesPerTurn)
        assertEquals(11, playerMoveState.matchesInHeap)
        assertEquals(false, playerMoveState.isGameOver)
        assertEquals(PlayerType.COMPUTER, playerMoveState.currentPlayer)
        assertEquals(MOVE_SUCCESSFUL, playerMoveState.message)
        assertNull(playerMoveState.winner)
        assertFalse(playerMoveState.isGameOver)
    }

    @Test
    fun testInvalidPlayerMove() {
        val startGameState = gameService.startGame(13, 3)
        assertThrows<InvalidMoveException> {
            gameService.playerMove(startGameState.gameId!!, -1)
        }
    }

    @Test
    fun testPlayerTurnCheck() {
        val startGameState = gameService.startGame(13, 3)
        gameService.playerMove(startGameState.gameId!!, 3)
        assertThrows<InvalidMoveException> {
            gameService.playerMove(startGameState.gameId!!, 1)
        }
    }

    @Test
    fun testPlayerMoveOnFinishGame() {
        val gameState = gameRepository.save(
            GameState(isGameOver = true)
        )

        val playerMoveState = gameService.playerMove(gameState.gameId!!, 3)

        assertNotNull(playerMoveState)
        assertEquals(gameState.gameId, playerMoveState.gameId)
        assertEquals(gameState.totalMatches, playerMoveState.totalMatches)
        assertEquals(gameState.maxMatchesPerTurn, playerMoveState.maxMatchesPerTurn)
        assertEquals(gameState.matchesInHeap, playerMoveState.matchesInHeap)
        assertEquals(gameState.currentPlayer, playerMoveState.currentPlayer)
        assertTrue(playerMoveState.isGameOver)
    }

    @Test
    fun testTakeMoreMatchedThenCurrentAvailable() {
        val startGameState = gameService.startGame(2,10)
        assertThrows<InvalidMoveException> {
            gameService.playerMove(startGameState.gameId!!, 3)
        }
    }

    @Test
    fun testValidComputerMove() {
        `when`(randomNumberGenerator.generateRandomNumber(anyInt(), anyInt()))
            .thenReturn(1)

        val startGameState = gameService.startGame(13, 1)
        val playerMoveState = gameService.playerMove(startGameState.gameId!!, 1)
        val computerMoveState = gameService.computerMove(startGameState.gameId!!)

        assertNotNull(computerMoveState)
        assertEquals(startGameState.gameId, computerMoveState.gameId)
        assertEquals(13, computerMoveState.totalMatches)
        assertEquals(1, computerMoveState.maxMatchesPerTurn)
        assertEquals(11, computerMoveState.matchesInHeap)
        assertEquals(false, computerMoveState.isGameOver)
        assertEquals(PlayerType.PLAYER, computerMoveState.currentPlayer)
        assertEquals(MOVE_SUCCESSFUL, computerMoveState.message)
        assertNull(playerMoveState.winner)
        assertFalse(playerMoveState.isGameOver)
    }

    @Test
    fun testInvalidComputerMove() {
        `when`(randomNumberGenerator.generateRandomNumber(anyInt(), anyInt()))
            .thenReturn(-4)

        val startGameState = gameService.startGame(13, 3)
        gameService.playerMove(startGameState.gameId!!, 3)

        assertThrows<InvalidMoveException> {
            gameService.computerMove(startGameState.gameId!!)
        }
    }

    @Test
    fun testTakeMoreMatchedThenCurrentAvailableForComputerPlay() {
        `when`(randomNumberGenerator.generateRandomNumber(anyInt(), anyInt()))
            .thenReturn(3)

        val startGameState = gameService.startGame(4,10)
        gameService.playerMove(startGameState.gameId!!, 3)

        assertThrows<InvalidMoveException> {
           gameService.computerMove(startGameState.gameId!!)
        }
    }

}