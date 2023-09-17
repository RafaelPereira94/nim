package pt.holisticon.nim.extension

import pt.holisticon.nim.domain.dto.GameStateDto
import pt.holisticon.nim.domain.entity.GameState

object GameStateExtension {

    fun GameState.toDto(): GameStateDto {
        return GameStateDto(
            totalMatches = this.totalMatches,
            maxMatchesPerTurn = this.maxMatchesPerTurn,
            matchesInHeap = this.matchesInHeap,
            currentPlayer = this.currentPlayer,
            isGameOver = this.isGameOver,
            winner = this.winner,
            gameId = this.gameId,
            message = this.message
        )
    }

    fun GameStateDto.toEntity(): GameState {
        return GameState(
            totalMatches = this.totalMatches,
            maxMatchesPerTurn = this.maxMatchesPerTurn,
            matchesInHeap = this.matchesInHeap,
            currentPlayer = this.currentPlayer,
            isGameOver = this.isGameOver,
            winner = this.winner,
            message = this.message
        ).also {
            it.gameId = this.gameId
        }
    }
}