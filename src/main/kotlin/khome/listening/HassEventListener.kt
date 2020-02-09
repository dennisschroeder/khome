package khome.listening

import khome.core.KhomeComponent
import khome.core.eventHandling.EventData
import khome.core.eventHandling.HassEvent
import org.koin.core.get
import java.util.UUID

inline fun <reified EventType : HassEvent> KhomeComponent.onHassEvent(noinline callback: suspend (EventData) -> Unit): LifeCycleHandler =
    registerHassEventCallback<EventType>(callback)

inline fun <reified EventType : HassEvent> KhomeComponent.registerHassEventCallback(
    noinline callback: suspend (EventData) -> Unit
): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val event = get<EventType>()
    event.subscribe(handle, callback)
    return LifeCycleHandler(handle)
}
