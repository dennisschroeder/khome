package khome.core.entities.inputDateTime

import khome.core.entities.EntitySubject
import khome.core.getAttribute
import java.time.LocalDate

abstract class DateEntity(name: String) :
    EntitySubject<String>("input_datetime", name) {

    init {
        val hasDate = state.getAttribute<Boolean>("has_date")
        check(hasDate) { "This entity is not configured to be an Date." }
    }

    val date get(): LocalDate = LocalDate.parse(state.state as String)
}
