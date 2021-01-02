package khome.core.mapping.adapter.default

import khome.core.mapping.KhomeTypeAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class LocalDateTimeAdapter : KhomeTypeAdapter<LocalDateTime> {
    override fun <P> from(value: P): LocalDateTime {
        return LocalDateTime
            .parse(
                value as String,
                DateTimeFormatter.ofPattern("y-M-d H:m:s")
            )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P> to(value: LocalDateTime): P {
        return LocalDateTime.of(
            value.year,
            value.monthValue,
            value.dayOfMonth,
            value.hour,
            value.minute,
            value.second
        ).toString() as P
    }
}
