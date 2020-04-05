package khome.core.entities.inputDateTime

import khome.core.entities.AbstractEntity
import khome.core.entities.getAttribute
import java.time.LocalDate

abstract class AbstractDateEntity(name: String) :
    AbstractEntity<String>("input_datetime", name) {

    init {
        val hasDate = newState.getAttribute<Boolean>("has_date")
        check(hasDate) { "This entity is not configured to be an Date." }
    }

    val date get(): LocalDate = LocalDate.parse(newState.state as String)
}
