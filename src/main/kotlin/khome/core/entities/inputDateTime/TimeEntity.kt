package khome.core.entities.inputDateTime

import khome.core.entities.EntityId
import khome.core.entities.EntitySubject
import khome.core.safeGet
import java.time.LocalTime

abstract class TimeEntity(name: String) :
    EntitySubject<String>(EntityId("input_datetime", name)) {

    init {
        val hasTime: Boolean = attributes.safeGet("has_time")
        check(hasTime) { "This entity is not configured to be Time." }
    }

    val time get(): LocalTime = LocalTime.parse(stateValue)
}
