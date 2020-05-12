package khome.core.entities.inputDateTime

import khome.core.entities.EntityId
import khome.core.entities.EntitySubject
import khome.core.safeGet
import java.time.LocalDateTime

abstract class DateTimeEntity(name: String) :
    EntitySubject<String>(EntityId("input_datetime", name)) {

    init {
        val hasTime: Boolean = attributes.safeGet("has_time")
        val hasDate: Boolean = attributes.safeGet("has_date")

        check(hasTime && hasDate) { "This entity is not configured to be an DateTime." }
    }

    val dateTime get(): LocalDateTime = LocalDateTime.parse(stateValue)
}
