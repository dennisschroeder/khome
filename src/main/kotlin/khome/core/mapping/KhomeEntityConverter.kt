package khome.core.mapping

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import khome.core.entities.EntityInterface
import khome.core.mapping.exceptions.ConvertToEntityException

class KhomeEntityConverter : TypeAdapter<EntityInterface>() {
    override fun write(jsonWriter: JsonWriter, value: EntityInterface?) {
        jsonWriter.value(value.toString())
    }

    override fun read(jsonReader: JsonReader): EntityInterface {
        throw ConvertToEntityException("There is no need to convert in this direction")
    }
}
