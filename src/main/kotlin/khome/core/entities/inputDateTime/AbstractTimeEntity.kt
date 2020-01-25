package khome.core.entities.inputDateTime

import khome.core.entities.AbstractEntity
import java.time.LocalTime

abstract class AbstractTimeEntity(name: String) :
    AbstractEntity<String>("input_datetime", name) {

    init {
        val hasTime = getAttributeValue<Boolean>("has_time")
        check(hasTime) { "This entity is not configured to be Time." }
    }

    val time get(): LocalTime = LocalTime.parse(stateValue)
}
