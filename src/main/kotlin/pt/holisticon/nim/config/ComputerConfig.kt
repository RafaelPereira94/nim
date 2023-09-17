package pt.holisticon.nim.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("computer")
@Component
class ComputerConfig {
    var random: Boolean? = true
}