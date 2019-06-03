package khome.core.entities.inputDateTime

import khome.core.entities.AbstractEntity
import java.time.LocalDate

abstract class AbstractDateEntity(name: String) :
    AbstractEntity<String>("input_datetime", name) {

    val date get(): LocalDate {
        val date = stateValue
        return LocalDate.parse(date)
    }
}