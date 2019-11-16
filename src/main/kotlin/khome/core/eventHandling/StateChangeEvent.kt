package khome.core.eventHandling

import khome.core.EventResult

class StateChangeEvent(private val delegate: Event<EventResult>) :
    Iterable<MutableMap.MutableEntry<String, Handler<EventResult>>> by delegate, EventInterface<EventResult> {
    override val listenerCount get() = delegate.listeners.size
    override fun subscribe(handle: String?, callback: EventResult.() -> Unit) {
        if (handle == null) delegate += callback else delegate[handle] = callback
    }

    override fun unsubscribe(handle: String) {
        delegate -= handle
    }
    override fun emit(eventData: EventResult) = delegate(eventData)
}
