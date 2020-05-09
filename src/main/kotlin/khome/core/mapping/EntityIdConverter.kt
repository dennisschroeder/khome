package khome.core.mapping

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import khome.core.entities.EntityId

class EntityIdConverter : TypeAdapter<EntityId>() {
    override fun write(jsonWriter: JsonWriter, value: EntityId) {
        jsonWriter.value(value.toString())
    }

    override fun read(jsonReader: JsonReader): EntityId {
        val nextString = jsonReader.nextString()
        return EntityId.fromString(nextString)
    }
}
