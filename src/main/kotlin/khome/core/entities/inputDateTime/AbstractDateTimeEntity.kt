package khome.core.entities.inputDateTime

import khome.core.entities.AbstractEntity
import java.time.LocalDateTime

abstract class AbstractDateTimeEntity(name: String) :
    AbstractEntity<String>("input_datetime", name) {

    init {
        val hasTime = getAttributeValue<Boolean>("has_time")
        val hasDate = getAttributeValue<Boolean>("has_date")

        check(hasTime && hasDate) { "This entity is not configured to be an DateTime." }
    }

    val dateTime get(): LocalDateTime = LocalDateTime.parse(stateValue)
}
