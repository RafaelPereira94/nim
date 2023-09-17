package pt.holisticon.nim.repository

import org.springframework.data.repository.CrudRepository
import pt.holisticon.nim.domain.entity.GameState

interface GameRepository : CrudRepository<GameState, String> {}