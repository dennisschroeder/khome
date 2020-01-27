package khome.calling

import khome.KhomeSession
import khome.core.MessageInterface
import khome.core.dependencyInjection.CallerID
import khome.core.dependencyInjection.ServiceCoroutineContext
import khome.core.eventHandling.CustomEvent
import khome.core.logger
import kotlinx.coroutines.launch
import org.koin.core.get
import org.koin.core.inject
import java.time.OffsetDateTime

inline fun <reified EmitterType : CustomEventEmitter> KhomeSession.fireCustomEvent(crossinline mutate: EmitterType.() -> Unit) {
    val serviceCoroutineContext: ServiceCoroutineContext by inject()
    val eventPayload: EmitterType by inject()

    launch(serviceCoroutineContext) {
        eventPayload.apply(mutate)
        eventPayload.id = get<CallerID>().incrementAndGet()
        // callWebSocketApi(eventPayload.toJson())
        logger.info { "(Sandbox) Emitted event with: " + eventPayload.toJson() }
    }
}

abstract class CustomEventEmitter(event: CustomEvent) : MessageInterface {
    var id: Int = 0
    private val type = "fire_event"
    private val event = mutableMapOf(
        "data" to emptyMap<String, Any>(),
        "event_type" to event.eventType,
        "time_fired" to OffsetDateTime.now(),
        "origin" to "LOCAL"
    )
}
