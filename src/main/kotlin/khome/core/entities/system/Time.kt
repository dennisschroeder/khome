package khome.core.entities.system

import khome.core.entities.sensors.AbstractSensorEntity
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Time : AbstractSensorEntity("time") {
    val currentTime get() = stateValue
    private val timePattern: DateTimeFormatter = DateTimeFormatter.ofPattern("H:m")
    val currentLocalTime: LocalTime get() = LocalTime.parse(currentTime, timePattern)
}
