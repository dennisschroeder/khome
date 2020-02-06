package khome.core.eventHandling

import khome.core.dependencyInjection.KhomeComponent
import org.koin.core.get

typealias EventData = Map<String, Any>

abstract class HassEvent(eventName: String, delegate: Event<EventData> = Event()) : KhomeComponent(),
    HassEventInterface<EventData> {
    final override val eventType: String = eventName
    private val eventHandler = delegate

    init {
        registerInEventRegistry()
    }

    private fun registerInEventRegistry() = get<HassEventRegistry>().register(eventType, this)
    override val listenerCount get() = eventHandler.listeners.size
    override fun subscribe(handle: String?, callback: suspend EventData.() -> Unit) {
        if (handle == null) eventHandler += callback else eventHandler[handle] = callback
    }

    override fun unsubscribe(handle: String) {
        eventHandler -= handle
    }

    override suspend fun emit(eventData: EventData) = eventHandler(eventData)
}
