package khome.core.entities.inputDateTime

import khome.core.entities.AbstractEntity
import khome.core.entities.getAttribute
import java.time.LocalDateTime

abstract class AbstractDateTimeEntity(name: String) :
    AbstractEntity<String>("input_datetime", name) {

    init {
        val hasTime = newState.getAttribute<Boolean>("has_time")
        val hasDate = newState.getAttribute<Boolean>("has_date")

        check(hasTime && hasDate) { "This entity is not configured to be an DateTime." }
    }

    val dateTime get(): LocalDateTime = LocalDateTime.parse(newState.state as String)
}
