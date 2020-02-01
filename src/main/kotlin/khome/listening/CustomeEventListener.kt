package khome.listening

import khome.KhomeSession
import khome.core.eventHandling.HassEvent
import khome.core.eventHandling.EventData
import org.koin.core.get
import java.util.UUID

inline fun <reified EventType : HassEvent> KhomeSession.onCustomEvent(noinline callback: (EventData) -> Unit): LifeCycleHandler =
    registerCustomEventCallback<EventType>(callback)

inline fun <reified EventType : HassEvent> KhomeSession.registerCustomEventCallback(
    noinline callback: (EventData) -> Unit
): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val event = get<EventType>()
    event.subscribe(handle, callback)
    return LifeCycleHandler(handle)
}
