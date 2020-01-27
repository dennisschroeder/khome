package khome.core.entities.system

import khome.core.entities.sensors.AbstractSensorEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTime : AbstractSensorEntity("date_time_iso") {
    private val currentDateTime get() = stateValue
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    val currentLocalDateTime: LocalDateTime get() = LocalDateTime.parse(currentDateTime, dateTimeFormatter)
}
