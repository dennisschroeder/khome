package khome.core.entities.inputDateTime

import khome.core.entities.AbstractEntity
import java.time.LocalDate

abstract class AbstractDateEntity(name: String) :
    AbstractEntity("input_datetime", name) {

    val date get(): LocalDate {
        val date = getStateValue<String>()
        return LocalDate.parse(date)
    }
}