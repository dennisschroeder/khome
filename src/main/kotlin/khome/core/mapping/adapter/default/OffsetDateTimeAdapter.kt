package khome.core.mapping.adapter.default

import khome.core.mapping.KhomeTypeAdapter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class OffsetDateTimeAdapter : KhomeTypeAdapter<OffsetDateTime> {
    override fun <P> from(value: P): OffsetDateTime {
        return OffsetDateTime.parse(value as String, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P> to(value: OffsetDateTime): P {
        return value.toString() as P
    }
}
