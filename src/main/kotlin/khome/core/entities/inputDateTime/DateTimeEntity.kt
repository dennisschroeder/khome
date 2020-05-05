package khome.core.entities.inputDateTime

import khome.core.entities.EntitySubject
import khome.core.getAttribute
import java.time.LocalDateTime

abstract class DateTimeEntity(name: String) :
    EntitySubject<String>("input_datetime", name) {

    init {
        val hasTime = state.getAttribute<Boolean>("has_time")
        val hasDate = state.getAttribute<Boolean>("has_date")

        check(hasTime && hasDate) { "This entity is not configured to be an DateTime." }
    }

    val dateTime get(): LocalDateTime = LocalDateTime.parse(state.state as String)
}
