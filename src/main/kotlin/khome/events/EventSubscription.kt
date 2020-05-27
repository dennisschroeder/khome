package khome.events

import com.google.gson.JsonElement
import khome.core.mapping.ObjectMapper
import kotlin.reflect.KClass

internal class EventSubscription(private val mapper: ObjectMapper, private val eventDataType: KClass<*>) {
    private val eventHandler = mutableListOf<EventHandler<*>>()
    fun attachEventHandler(handler: EventHandler<*>) = eventHandler.add(handler)
    fun invokeEventHandler(eventData: JsonElement) {
        val mappedEventData = mapper.fromJson(eventData, eventDataType.java)
        eventHandler.forEach { handler -> (handler as EventHandler<Any>).handle(mappedEventData) }
    }
}
