package khome.core.mapping.adapter.default

import khome.core.mapping.KhomeTypeAdapter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeAdapter : KhomeTypeAdapter<LocalTime> {
    override fun <P> from(value: P): LocalTime {
        return LocalTime
            .parse(
                value as String,
                DateTimeFormatter.ISO_TIME
            )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P> to(value: LocalTime): P {
        return value.toString() as P
    }
}
