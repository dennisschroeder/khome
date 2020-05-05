package khome.core.mapping

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import khome.core.entities.EntitySubjectInterface
import khome.core.mapping.exceptions.ConvertToEntityException

class KhomeEntityConverter : TypeAdapter<EntitySubjectInterface>() {
    override fun write(jsonWriter: JsonWriter, value: EntitySubjectInterface?) {
        jsonWriter.value(value.toString())
    }

    override fun read(jsonReader: JsonReader): EntitySubjectInterface {
        throw ConvertToEntityException("There is no need to convert in this direction")
    }
}
