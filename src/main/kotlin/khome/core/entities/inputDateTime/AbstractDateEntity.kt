package khome.core.entities.inputDateTime

import khome.core.entities.AbstractEntity
import java.time.LocalDate
import java.time.LocalTime

abstract class AbstractDateEntity(dateTimeEntityName: String) :
    AbstractEntity("input_datetime", dateTimeEntityName) {

    val date get(): LocalDate {
        val date = getStateValue<String>()
        return LocalDate.parse(date)
    }
}