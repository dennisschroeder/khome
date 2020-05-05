package khome.core.entities.system

import khome.core.entities.sensors.SensorEntity
import mu.KLogger
import mu.KotlinLogging
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Time : SensorEntity("time") {
    val logger: KLogger = KotlinLogging.logger {}
    private val currentTime get() = state as String
    private val timePattern: DateTimeFormatter
        get() = DateTimeFormatter.ofPattern("H:m")
    val currentLocalTime: LocalTime get() = LocalTime.parse(currentTime, timePattern)
}
