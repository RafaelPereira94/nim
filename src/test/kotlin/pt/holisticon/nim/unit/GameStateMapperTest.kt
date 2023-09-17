package pt.holisticon.nim.unit

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import pt.holisticon.nim.domain.dto.GameStateDto
import pt.holisticon.nim.domain.entity.GameState
import pt.holisticon.nim.domain.enums.PlayerType
import pt.holisticon.nim.extension.GameStateExtension.toDto
import pt.holisticon.nim.extension.GameStateExtension.toEntity

class GameStateMapperTest {

    @Test
    fun `should map gameState entity to Dto`() {
        // Arrange
        val gameState = GameState(
            totalMatches = 13,
            maxMatchesPerTurn = 3,
            matchesInHeap = 10,
            currentPlayer = PlayerType.PLAYER,
            isGameOver = false,
            winner = null,
            message = "Game in progress"
        ).apply {
            this.gameId = "game123"
        }

        // Act
        val gameStateDto = gameState.toDto()

        // Assert
        Assertions.assertEquals(13, gameStateDto.totalMatches)
        Assertions.assertEquals(3, gameStateDto.maxMatchesPerTurn)
        Assertions.assertEquals(10, gameStateDto.matchesInHeap)
        Assertions.assertEquals(PlayerType.PLAYER, gameStateDto.currentPlayer)
        Assertions.assertEquals(false, gameStateDto.isGameOver)
        Assertions.assertEquals(null, gameStateDto.winner)
        Assertions.assertEquals("game123", gameStateDto.gameId)
        Assertions.assertEquals("Game in progress", gameStateDto.message)
    }

    @Test
    fun `should map gameState Dto to Entity`() {
        // Arrange
        val gameStateDto = GameStateDto(
            totalMatches = 20,
            maxMatchesPerTurn = 2,
            matchesInHeap = 18,
            currentPlayer = PlayerType.COMPUTER,
            isGameOver = true,
            winner = PlayerType.PLAYER,
            gameId = "game456",
            message = "Player won"
        )

        // Act
        val gameState = gameStateDto.toEntity()

        // Assert
        Assertions.assertEquals(20, gameState.totalMatches)
        Assertions.assertEquals(2, gameState.maxMatchesPerTurn)
        Assertions.assertEquals(18, gameState.matchesInHeap)
        Assertions.assertEquals(PlayerType.COMPUTER, gameState.currentPlayer)
        Assertions.assertEquals(true, gameState.isGameOver)
        Assertions.assertEquals(PlayerType.PLAYER, gameState.winner)
        Assertions.assertEquals("Player won", gameState.message)
    }
}
