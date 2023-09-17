package pt.holisticon.nim.controller

import jakarta.websocket.server.PathParam
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.holisticon.nim.domain.dto.GameStateDto
import pt.holisticon.nim.exception.EntityNotFoundException
import pt.holisticon.nim.exception.InvalidMoveException
import pt.holisticon.nim.exception.InvalidParametersException
import pt.holisticon.nim.extension.GameStateExtension.toEntity
import pt.holisticon.nim.service.GameService

@RestController
class GameController(val gameService: GameService) {

    @PostMapping("/start-game")
    fun startGame(
        @RequestParam totalMatches: Int,
        @RequestParam maxMatchesPerTurn: Int
    ): ResponseEntity<GameStateDto> {
        //Initialize a new game with custom settings
        return try {
            ResponseEntity.ok(gameService.startGame(totalMatches, maxMatchesPerTurn))
        }catch (e: InvalidParametersException) {
            val gameStateDto = GameStateDto(
                message = e.message
            )
            ResponseEntity.badRequest().body(gameStateDto)
        }

    }

    @GetMapping("/state/{id}")
    fun getGameState(@PathVariable("id") id: String): ResponseEntity<GameStateDto> {
        //Fetches the game state from database.
        return try {
            val gameState = gameService.getGameState(id)
            ResponseEntity.ok(gameState)
        } catch (e: EntityNotFoundException) {
            ResponseEntity.notFound().build()
        }
    }

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