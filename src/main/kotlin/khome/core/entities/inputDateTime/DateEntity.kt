package khome.core.entities.inputDateTime

import khome.core.entities.EntityId
import khome.core.entities.EntitySubject
import khome.core.safeGet
import java.time.LocalDate

abstract class DateEntity(name: String) :
    EntitySubject<String>(EntityId("input_datetime", name)) {

    init {
        val hasDate: Boolean = attributes.safeGet("has_date")
        check(hasDate) { "This entity is not configured to be an Date." }
    }

    val date
        get(): LocalDate = LocalDate.parse(stateValue)
}
