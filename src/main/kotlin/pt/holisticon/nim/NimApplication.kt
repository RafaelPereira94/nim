package pt.holisticon.nim

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NimApplication

fun main(args: Array<String>) {
    runApplication<NimApplication>(*args)
}
