package khome.core.eventHandling

import khome.core.EventResult

class StateChangeEvent(delegate: Event<EventResult>) :
    Iterable<MutableMap.MutableEntry<String, Handler<EventResult>>> by delegate, EventInterface<EventResult> {
    private val eventHandler = delegate

    override val listenerCount get() = eventHandler.listeners.size
    override fun subscribe(handle: String?, callback: suspend EventResult.() -> Unit) {
        if (handle == null) eventHandler += callback else eventHandler[handle] = callback
    }

    override fun unsubscribe(handle: String) {
        eventHandler -= handle
    }

    override suspend fun emit(eventData: EventResult) = eventHandler(eventData)
}
