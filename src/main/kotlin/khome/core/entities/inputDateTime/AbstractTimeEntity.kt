package khome.core.entities.inputDateTime

import khome.core.entities.AbstractEntity
import java.time.LocalTime

abstract class AbstractTimeEntity(name: String) :
    AbstractEntity<String>("input_datetime", name) {

    val time get(): LocalTime = LocalTime.parse(stateValue)
}
