package khome.core.entities.inputDateTime

import khome.core.entities.AbstractEntity
import java.time.LocalDate

abstract class AbstractDateEntity(name: String) :
    AbstractEntity<String>("input_datetime", name) {

    init {
        val hasDate = getAttributeValue<Boolean>("has_date")
        check(hasDate) { "This entity is not configured to be an Date." }
    }

    val date get(): LocalDate = LocalDate.parse(stateValue)
}
