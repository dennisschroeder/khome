package khome.core.mapping

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class OffsetDateTimeAdapter : TypeAdapter<OffsetDateTime>() {
    override fun write(jsonWriter: JsonWriter, offsetDateTime: OffsetDateTime) {
        jsonWriter.value(offsetDateTime.toString())
    }

    override fun read(jsonReader: JsonReader): OffsetDateTime {
        val nextString = jsonReader.nextString()
        return OffsetDateTime
            .parse(
                nextString,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
    }
}
