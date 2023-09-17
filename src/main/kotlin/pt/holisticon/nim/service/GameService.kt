package pt.holisticon.nim.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pt.holisticon.nim.domain.dto.GameStateDto
import pt.holisticon.nim.domain.entity.GameState
import pt.holisticon.nim.domain.enums.PlayerType
import pt.holisticon.nim.exception.EntityNotFoundException
import pt.holisticon.nim.exception.InvalidMoveException
import pt.holisticon.nim.exception.InvalidParametersException
import pt.holisticon.nim.extension.GameStateExtension.toDto
import pt.holisticon.nim.extension.GameStateExtension.toEntity
import pt.holisticon.nim.repository.GameRepository
import pt.holisticon.nim.util.Constant.COMPUTER_WINNER
import pt.holisticon.nim.util.Constant.GAME_RESET
import pt.holisticon.nim.util.Constant.MOVE_SUCCESSFUL
import pt.holisticon.nim.util.Constant.PLAYER_WINNER
import pt.holisticon.nim.util.RandomNumberGenerator

private const val DEFAULT_NUMBER_MATCHES = 13
private const val DEFAULT_NUMBER_OF_MATCHES_PER_TURN = 3

@Service
class GameService(val gameRepository: GameRepository, val randomNumberGenerator: RandomNumberGenerator) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(GameService::class.java)
    }

    /**
     * Starts a new game with the specified parameters.
     *
     * @param totalMatches The total number of matches in the game.
     * @param maxMatchesPerTurn The maximum number of matches a player can remove per turn.
     * @return A [GameStateDto] representing the initial state of the game.
     * @throws InvalidParametersException If the provided parameters are invalid (totalMatches <= 0 or maxMatchesPerTurn <= 0).
     */
    fun startGame(totalMatches: Int, maxMatchesPerTurn: Int): GameStateDto {
        logger.info("Creating new game with $totalMatches total matches and $maxMatchesPerTurn max matches per turn")

        if(totalMatches <= 0 || maxMatchesPerTurn <= 0) {
            logger.error("Invalid parameters to start a game. totalMatches: $totalMatches and maxMatchesPerTurn: $maxMatchesPerTurn")
            throw InvalidParametersException("Invalid parameters to start a game. totalMatches: $totalMatches and maxMatchesPerTurn: $maxMatchesPerTurn")
        }

        val gameState = gameRepository.save(
            GameState(
                totalMatches = totalMatches,
                maxMatchesPerTurn = maxMatchesPerTurn
            )
        )

        return gameState.toDto()
    }

    /**
     * Retrieves the game state for a given game ID.
     *
     * @param id The unique identifier of the game.
     * @return A [GameStateDto] representing the current state of the game.
     * @throws EntityNotFoundException If the game with the specified ID is not found.
     */
    fun getGameState(id: String): GameStateDto {
        logger.info("Getting game state for id $id")
        val gameState = gameRepository.findById(id)
        return gameState.map {
            it.toDto()
        }.orElseThrow {
            logger.error("Game id $id not found")
            EntityNotFoundException("Cannot find game state with $id id")
        }
    }

    /**
     * Resets a game with the specified ID to its initial state.
     *
     * @param id The unique identifier of the game to be reset.
     * @return A [GameStateDto] representing the game in its reset state.
     * @throws EntityNotFoundException If the game with the specified ID is not found.
     */
    fun resetGame(id: String): GameStateDto {
        logger.info("Reset game with id $id")
        val gameState = gameRepository.findById(id)
        return if (gameState.isPresent) {
            val game = gameState.get()
            val copy = game.copy(
                maxMatchesPerTurn = DEFAULT_NUMBER_OF_MATCHES_PER_TURN,
                matchesInHeap = DEFAULT_NUMBER_MATCHES,
                currentPlayer = PlayerType.PLAYER,
                isGameOver = false,
                totalMatches = DEFAULT_NUMBER_MATCHES,
                winner = null,
                message = GAME_RESET
            )
            copy.gameId = game.gameId
            gameRepository.save(copy).toDto()
        } else {
            logger.error("Game id $id not found")
            throw EntityNotFoundException("Cannot find game state with $id id")
        }
    }

    /**
     * Represents a player's move in the game.
     *
     * @param id The unique identifier of the game.
     * @param playerMoves The number of matches the player wants to remove in this move.
     * @return A [GameStateDto] representing the updated state of the game after the player's move.
     * @throws InvalidMoveException If the move is invalid, such as being out of turn, taking too many or too few matches, or trying to move after the game is over.
     * @throws EntityNotFoundException If the game with the specified ID is not found.
     */
    fun playerMove(id: String, playerMoves: Int): GameStateDto {
        logger.info("Starting player move with id $id and player moves $playerMoves")
        val currentGameState = getGameState(id).toEntity()

        if (!isPlayerTurn(currentGameState)) { //Not player turn
            logger.error("Not player turn on game id $id")
            throw InvalidMoveException("Invalid move, computer turn of playing.")
        }

        if (isGameFinished(currentGameState)) {
            logger.error("Game id $id already finished")
            return currentGameState.toDto() //return the state already provided by the database since It's already over.
        }

        if (playerMoves < 1 || playerMoves > currentGameState.maxMatchesPerTurn) {
            logger.error("Invalid move on game id $id. We can't take more or less than allowed")
            throw InvalidMoveException("The number of matches taken must be between 1 or ${currentGameState.maxMatchesPerTurn}")
        }

        if (playerMoves > currentGameState.matchesInHeap) {
            logger.error("Invalid move on game id $id. Number of matches taken are above the number of matches in heap.")
            throw InvalidMoveException("Number of matches taken $playerMoves are above the number of matches in heap ${currentGameState.matchesInHeap}")
        }

        //Update State
        currentGameState.matchesInHeap -= playerMoves

        if (isGameOver(currentGameState)) { //number of matches == 0 current player loses
            logger.info("Game-over on game id $id")
            currentGameState.isGameOver = true
            currentGameState.winner = PlayerType.COMPUTER
            currentGameState.message = COMPUTER_WINNER
        }

        currentGameState.currentPlayer = PlayerType.COMPUTER
        currentGameState.message = MOVE_SUCCESSFUL
        currentGameState.gameId = id
        logger.info("$MOVE_SUCCESSFUL on game with id $id")
        return gameRepository.save(currentGameState).toDto()
    }

    /**
     * Represents the computer's move in the game.
     *
     * @param id The unique identifier of the game.
     * @return A [GameStateDto] representing the updated state of the game after the computer's move.
     * @throws InvalidMoveException If the move is invalid, such as being out of turn, taking too many or too few matches, or trying to move after the game is over.
     * @throws EntityNotFoundException If the game with the specified ID is not found.
     */
    fun computerMove(id: String): GameStateDto {
        val currentGameState = getGameState(id).toEntity()

        if (isPlayerTurn(currentGameState)) { //Not Computer turn
            throw InvalidMoveException("Invalid move, Player turn of playing.")
        }

        if (isGameFinished(currentGameState)) {
            return currentGameState.toDto()
        }

        //Get random computer move based on max number
        val computerMove = randomNumberGenerator.generateRandomNumber(1, currentGameState.maxMatchesPerTurn + 1)

        if (computerMove < 1 || computerMove > currentGameState.matchesInHeap || computerMove > currentGameState.maxMatchesPerTurn) {
            throw InvalidMoveException("Number of matches taken $computerMove are above the number of matches in heap ${currentGameState.matchesInHeap} or below 1")
        }

        currentGameState.matchesInHeap -= computerMove

        if (isGameOver(currentGameState)) {
            currentGameState.isGameOver = true
            currentGameState.winner = PlayerType.PLAYER
            currentGameState.message = PLAYER_WINNER
        }

        currentGameState.currentPlayer = PlayerType.PLAYER
        currentGameState.message = MOVE_SUCCESSFUL
        currentGameState.gameId = id
        return gameRepository.save(currentGameState).toDto()
    }

    private fun isPlayerTurn(currentGameState: GameState): Boolean {
        return currentGameState.currentPlayer == PlayerType.PLAYER
    }

    private fun isGameFinished(gameState: GameState): Boolean {
        return gameState.isGameOver
    }

    private fun isGameOver(gameState: GameState): Boolean {
        return gameState.matchesInHeap == 0
    }

}
