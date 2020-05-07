package khome.core.entities.inputDateTime

import khome.core.entities.EntitySubject
import khome.core.getAttribute
import java.time.LocalTime

@ExperimentalStdlibApi
abstract class TimeEntity(name: String) :
    EntitySubject<String>("input_datetime", name) {

    init {
        val hasTime = state.getAttribute<Boolean>("has_time")
        check(hasTime) { "This entity is not configured to be Time." }
    }

    val time get(): LocalTime = LocalTime.parse(state.state as String)
}
