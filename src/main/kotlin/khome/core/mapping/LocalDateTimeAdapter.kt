package khome.core.mapping

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeAdapter : TypeAdapter<LocalDateTime>() {
    override fun write(jsonWriter: JsonWriter, date: LocalDateTime) {
        jsonWriter.value(LocalDateTime.of(date.year, date.monthValue, date.dayOfMonth, date.hour, date.minute, date.second).toString())
    }

    override fun read(jsonReader: JsonReader): LocalDateTime {
        val nextString = jsonReader.nextString()
        return LocalDateTime
            .parse(
                nextString,
                DateTimeFormatter.ofPattern("y-M-d H:m:s")
            )
    }
}
