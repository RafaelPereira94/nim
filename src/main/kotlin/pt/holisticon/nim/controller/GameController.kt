package pt.holisticon.nim.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.holisticon.nim.domain.dto.GameStateDto
import pt.holisticon.nim.exception.EntityNotFoundException
import pt.holisticon.nim.exception.InvalidMoveException
import pt.holisticon.nim.exception.InvalidParametersException
import pt.holisticon.nim.service.GameService

@Tag(name = "Game Controller", description = "The Game controller Api")
@RestController
class GameController(val gameService: GameService) {

    @Operation(
        summary = "Start Nim Game",
        description = "Creates a new nim game with parameters provided."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful start game."),
        ApiResponse(responseCode = "400", description = "Invalid parameters provided.")
    )
    @PostMapping("/start-game", produces = (["application/json"]))
    fun startGame(
        @RequestParam totalMatches: Int,
        @RequestParam maxMatchesPerTurn: Int
    ): ResponseEntity<GameStateDto> {
        //Initialize a new game with custom settings
        return try {
            ResponseEntity.ok(gameService.startGame(totalMatches, maxMatchesPerTurn))
        } catch (e: InvalidParametersException) {
            val gameStateDto = GameStateDto(
                message = e.message
            )
            ResponseEntity.badRequest().body(gameStateDto)
        }

    }

    @Operation(
        summary = "Fetch game state",
        description = "Fetches the game state for the game identification provided."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful fetching game state."),
        ApiResponse(responseCode = "404", description = "No game state found.")
    )
    @GetMapping("/state/{id}", produces = (["application/json"]))
    fun getGameState(@PathVariable("id") id: String): ResponseEntity<GameStateDto> {
        //Fetches the game state from database.
        return try {
            val gameState = gameService.getGameState(id)
            ResponseEntity.ok(gameState)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(
        summary = "Reset game",
        description = "Resets the game with id provided, with default configuration."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful resets the game."),
        ApiResponse(responseCode = "404", description = "No game state found.")
    )
    @PutMapping("/reset/{id}")
    fun resetGame(@PathVariable("id") id: String): ResponseEntity<GameStateDto> {
        //Resets the game to default state and in the beginning
        return try {
            val gameState = gameService.resetGame(id)
            ResponseEntity.ok(gameState)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

    @Operation(
        summary = "Make Player move",
        description = "This endpoint allows the player to take matches on game provided."
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful fetching game state."),
        ApiResponse(responseCode = "400", description = "Invalid move by the player based on playerMoves")
    )
    @PostMapping("/player-move/{id}/{playerMoves}")
    fun playerMove(
        @PathVariable("id") id: String,
        @PathVariable("playerMoves") playerMoves: Int
    ): ResponseEntity<GameStateDto> {
        return try {
            ResponseEntity.ok(gameService.playerMove(id, playerMoves))
        } catch (e: InvalidMoveException) {
            val currentState = gameService.getGameState(id)
            currentState.message = e.message

            ResponseEntity
                .badRequest()
                .body(currentState)
        }

    }
    @Operation(
        summary = "Make Computer move",
        description = "This endpoint does a computer move, if followed after player turn"
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Successful fetching game state."),
        ApiResponse(responseCode = "500", description = "If computer can't make a move.")
    )
    @PostMapping("/computer-move/{id}")
    fun computerMove(@PathVariable("id") id: String): ResponseEntity<GameStateDto> {
        return try {
            ResponseEntity.ok(gameService.computerMove(id))
        } catch (e: InvalidMoveException) {
            val currentState = gameService.getGameState(id)
            currentState.message = e.message

            ResponseEntity
                .internalServerError()
                .body(currentState)
        }
    }
}