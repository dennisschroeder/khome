package khome.core.mapping

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeAdapter : TypeAdapter<LocalTime>() {
    override fun write(jsonWriter: JsonWriter, time: LocalTime) {
        jsonWriter.value(LocalTime.of(time.hour, time.minute, time.second).toString())
    }

    override fun read(jsonReader: JsonReader): LocalTime {
        val nextString = jsonReader.nextString()
        return LocalTime
            .parse(
                nextString,
                DateTimeFormatter.ISO_TIME
            )
    }
}
