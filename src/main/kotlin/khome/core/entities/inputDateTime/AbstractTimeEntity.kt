package khome.core.entities.inputDateTime

import khome.core.entities.AbstractEntity
import java.time.LocalTime

abstract class AbstractTimeEntity(dateTimeEntityName: String) :
    AbstractEntity("input_datetime", dateTimeEntityName) {

    val time get(): LocalTime {
        val time = getStateValue<String>()
        return LocalTime.parse(time)
    }
}