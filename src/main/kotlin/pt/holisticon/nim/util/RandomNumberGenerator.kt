package pt.holisticon.nim.util

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class RandomNumberGenerator {
    fun generateRandomNumber(from: Int, until: Int): Int {
        return Random.nextInt(from, until)
    }
}