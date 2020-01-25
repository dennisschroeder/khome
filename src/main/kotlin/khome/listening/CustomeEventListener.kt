package khome.listening

import khome.KhomeSession
import khome.core.eventHandling.CustomEvent
import khome.core.eventHandling.EventData
import org.koin.core.get
import org.koin.core.qualifier.named
import java.util.UUID

fun KhomeSession.onCustomEvent(eventName: String, callback: (EventData) -> Unit) =
    registerCustomEventCallback(eventName, callback)

fun KhomeSession.registerCustomEventCallback(
    eventName: String,
    callback: (EventData) -> Unit
): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val event = get<CustomEvent>(named(eventName))
    event.subscribe(handle, callback)

    return LifeCycleHandler(handle)
}
