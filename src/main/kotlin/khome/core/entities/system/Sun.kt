package khome.core.entities.system

import khome.core.entities.AbstractEntity
import khome.core.entities.getAttribute
import mu.KLogger
import mu.KotlinLogging
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

class Sun : AbstractEntity<String>("sun", "sun") {
    val logger: KLogger = KotlinLogging.logger {}
    val isUp get() = newState.state == "above_horizon"
    val isDown get() = newState.state == "below_horizon"

    val nextSunrise get() = getNextSunPosition("next_rising")
    val nextSunset get() = getNextSunPosition("next_setting")

    private fun getNextSunPosition(nextPosition: String): LocalDateTime {
        val nextSunPositionChange = newState.getAttribute<String>(nextPosition)
        return convertUtcToLocalDateTime(nextSunPositionChange)
    }

    private fun convertUtcToLocalDateTime(utcDateTime: String): LocalDateTime {
        val offsetDateTime = OffsetDateTime.parse(utcDateTime)
        val zonedDateTime = offsetDateTime.atZoneSameInstant(ZoneId.systemDefault())
        return zonedDateTime.toLocalDateTime()
    }
}
