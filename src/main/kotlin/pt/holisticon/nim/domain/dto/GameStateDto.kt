package pt.holisticon.nim.domain.dto

import pt.holisticon.nim.domain.enums.PlayerType

class GameStateDto(
    var totalMatches: Int = 13,
    var maxMatchesPerTurn: Int = 3,
    var matchesInHeap: Int = totalMatches,
    var currentPlayer: PlayerType = PlayerType.PLAYER,
    var isGameOver: Boolean = false,
    var winner: PlayerType? = null,
    var gameId: String? = null,
    var message: String? = null
)