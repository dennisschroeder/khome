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
            is BooleanArray -> {
                outgoing.beginArray()
                result.forEach { outgoing.value(it) }
                outgoing.endArray()
            }
            is CharArray -> {
                outgoing.beginArray()
                result.forEach { outgoing.value(it.toString()) }
                outgoing.endArray()
            }
            is IntArray -> {
                outgoing.beginArray()
                result.forEach { outgoing.value(it) }
                outgoing.endArray()
            }
            is DoubleArray -> {
                outgoing.beginArray()
                result.forEach { outgoing.value(it) }
                outgoing.endArray()
            }
            is LongArray -> {
                outgoing.beginArray()
                result.forEach { outgoing.value(it) }
                outgoing.endArray()
            }
            else -> logger.error { "Could not write value in Gson. Value is ${value!!::class}" }
        }
    }

    override fun read(incoming: JsonReader): T =
        when (primitiveType) {
            String::class -> adapter.from(incoming.nextString())
            Int::class -> adapter.from(incoming.nextInt())
            Boolean::class -> adapter.from(incoming.nextBoolean())
            Long::class -> adapter.from(incoming.nextLong())
            Array<String>::class -> {
                incoming.beginArray()
                val list = mutableListOf<String>()
                while (incoming.hasNext()) {
                    list.add(incoming.nextString())
                }
                incoming.endArray()
                adapter.from(list)
            }
            Array<Int>::class -> {
                incoming.beginArray()
                val list = mutableListOf<Int>()
                while (incoming.hasNext()) {
                    list.add(incoming.nextInt())
                }
                incoming.endArray()
                adapter.from(list)
            }
            Array<Double>::class -> {
                incoming.beginArray()
                val list = mutableListOf<Double>()
                while (incoming.hasNext()) {
                    list.add(incoming.nextDouble())
                }
                incoming.endArray()
                adapter.from(list)
            }
            Double::class -> adapter.from(incoming.nextDouble())
            else -> throw IllegalStateException("${primitiveType.java.simpleName} can not be converted to ${incoming.peek()}. Please check your TypeConverter definition.")
        }
}
