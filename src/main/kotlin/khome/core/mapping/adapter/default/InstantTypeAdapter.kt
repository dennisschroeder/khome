package khome.core.mapping.adapter.default

import khome.core.mapping.KhomeTypeAdapter
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class InstantTypeAdapter : KhomeTypeAdapter<Instant> {
    override fun <P> from(value: P): Instant {
        return OffsetDateTime.parse(value as String, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <P> to(value: Instant): P {
        return value.toString() as P
    }
}
