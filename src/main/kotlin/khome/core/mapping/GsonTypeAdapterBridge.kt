package khome.core.mapping

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import mu.KotlinLogging
import kotlin.reflect.KClass

internal class GsonTypeAdapterBridge<T, P : Any>(
    private val adapter: KhomeTypeAdapter<T>,
    private val primitiveType: KClass<P>
) : TypeAdapter<T>() {
    private val logger = KotlinLogging.logger { }

    override fun write(outgoing: JsonWriter, value: T) {
        when (val result = adapter.to<P>(value)) {
            is Boolean -> outgoing.value(result as Boolean)
            is String -> outgoing.value(result as String)
            is Number -> outgoing.value(result as Number)
            is Long -> outgoing.value(result as Long)
            is Double -> outgoing.value(result as Double)
            else -> logger.error { "Could not write value in Gson. Value is ${value!!::class}" }
        }
    }

    override fun read(incoming: JsonReader): T =
        when (primitiveType) {
            String::class -> adapter.from(incoming.nextString())
            Double::class -> adapter.from(incoming.nextDouble())
            Int::class -> adapter.from(incoming.nextInt())
            Boolean::class -> adapter.from(incoming.nextBoolean())
            Long::class -> adapter.from(incoming.nextLong())
            else -> throw IllegalStateException("$primitiveType can not be converted")
        }
}
