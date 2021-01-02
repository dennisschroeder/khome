package khome.events

import com.google.gson.JsonElement
import khome.KhomeApplicationImpl
import khome.core.mapping.ObjectMapperInterface
import khome.errorHandling.EventHandlerExceptionHandler
import khome.observability.Switchable
import kotlin.reflect.KClass

internal class EventSubscription<ED>(
    private val app: KhomeApplicationImpl,
    private val mapper: ObjectMapperInterface,
    private val eventDataType: KClass<*>
) {
    private val eventHandler: MutableList<EventHandler<ED>> = mutableListOf()

    fun attachEventHandler(handler: EventHandlerFunction<ED>): Switchable =
        EventHandlerImpl(
            handler,
            EventHandlerExceptionHandler(app.eventHandlerExceptionHandlerFunction)
        ).also { eventHandler.add(it) }

    @Suppress("UNCHECKED_CAST")
    fun invokeEventHandler(eventData: JsonElement) {
        val mappedEventData: ED = mapper.fromJson(eventData, eventDataType.java) as ED
        eventHandler.forEach { handler -> handler.handle(mappedEventData) }
    }
}
