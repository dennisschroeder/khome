package khome.core.mapping

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class InstantAdapter : TypeAdapter<Instant>() {
    override fun write(jsonWriter: JsonWriter, instant: Instant) {
        jsonWriter.value(instant.toString())
    }

    override fun read(jsonReader: JsonReader): Instant {
        val nextString = jsonReader.nextString()
        return OffsetDateTime.parse(nextString, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant()
    }
}
