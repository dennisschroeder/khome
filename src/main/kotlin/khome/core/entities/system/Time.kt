package khome.core.entities.system

import khome.core.entities.sensors.AbstractSensorEntity
import mu.KLogger
import mu.KotlinLogging
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Time : AbstractSensorEntity("time") {
    val logger: KLogger = KotlinLogging.logger {}
    private val currentTime get() = newState.state as String
    private val timePattern: DateTimeFormatter = DateTimeFormatter.ofPattern("H:m")
    val currentLocalTime: LocalTime get() = LocalTime.parse(currentTime, timePattern)
}
