package pt.holisticon.nim.domain.entity

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import pt.holisticon.nim.domain.enums.PlayerType

@RedisHash("GameState")
data class GameState(
    var totalMatches: Int = 13,
    var maxMatchesPerTurn: Int = 3,
    var matchesInHeap: Int = totalMatches,
    var currentPlayer: PlayerType = PlayerType.PLAYER,
    var isGameOver: Boolean = false,
    var winner: PlayerType? = null,
    var message: String? = null
) {
    @get:Id
    var gameId: String? = null
}
