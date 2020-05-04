package khome.core.events

import khome.core.dependencyInjection.KhomeKoinComponent
import kotlinx.coroutines.flow.collect
import org.koin.core.get

typealias EventData = Map<String, Any>

abstract class HassEvent(
    final override val eventType: String
) : KhomeKoinComponent,
    EventInterface<EventData>, HassEventInterface {
    private val eventHandler = Event<EventData>()

    init {
        registerInEventRegistry()
    }

    private fun registerInEventRegistry() =
        get<HassEventRegistry>().register(eventType, this)

    override val listenerCount
        get() = eventHandler.listeners.size

    override fun subscribe(handle: String?, callback: (EventData) -> Unit) {
        if (handle == null) eventHandler += callback else eventHandler[handle] = callback
    }

    override fun unsubscribe(handle: String) {
        eventHandler -= handle
    }

    override suspend fun emit(eventData: EventData) =
        eventHandler.collect { handlerFunction ->
            handlerFunction(eventData)
        }
}
