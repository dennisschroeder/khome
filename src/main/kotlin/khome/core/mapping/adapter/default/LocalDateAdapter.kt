package khome.core.mapping.adapter.default

import khome.core.mapping.KhomeTypeAdapter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class LocalDateAdapter : KhomeTypeAdapter<LocalDate> {
    override fun <P> from(value: P): LocalDate {
        return LocalDate
            .parse(
                value as String,
                DateTimeFormatter.ISO_DATE
            )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P> to(value: LocalDate): P {
        return value.toString() as P
    }
}
