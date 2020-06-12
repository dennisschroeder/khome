package khome.core.mapping

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateAdapter : TypeAdapter<LocalDate>() {
    override fun write(jsonWriter: JsonWriter, date: LocalDate) {
        jsonWriter.value(date.toString())
    }

    override fun read(jsonReader: JsonReader): LocalDate {
        val nextString = jsonReader.nextString()
        return LocalDate
            .parse(
                nextString,
                DateTimeFormatter.ISO_DATE
            )
    }
}
