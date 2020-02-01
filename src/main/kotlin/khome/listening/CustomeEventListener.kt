package khome.listening

import khome.KhomeSession
import khome.core.eventHandling.CustomEvent
import khome.core.eventHandling.EventData
import org.koin.core.get
import java.util.UUID

inline fun <reified EventType : CustomEvent> KhomeSession.onCustomEvent(noinline callback: (EventData) -> Unit): LifeCycleHandler =
    registerCustomEventCallback<EventType>(callback)

inline fun <reified EventType : CustomEvent> KhomeSession.registerCustomEventCallback(
    noinline callback: (EventData) -> Unit
): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val event = get<EventType>()
    event.subscribe(handle, callback)
    return LifeCycleHandler(handle)
}
