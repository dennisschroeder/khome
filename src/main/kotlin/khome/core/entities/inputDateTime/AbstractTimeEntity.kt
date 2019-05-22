package khome.core.entities.inputDateTime

import khome.core.entities.AbstractEntity
import java.time.LocalTime

abstract class AbstractTimeEntity(name: String) :
    AbstractEntity("input_datetime", name) {

    val time get(): LocalTime {
        val time = getStateValue<String>()
        return LocalTime.parse(time)
    }
}