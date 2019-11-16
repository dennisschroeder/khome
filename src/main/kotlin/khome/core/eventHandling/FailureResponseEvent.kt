package khome.core.eventHandling

import khome.core.ErrorResult

internal class FailureResponseEvent(private val delegate: Event<ErrorResult>) : Iterable<MutableMap.MutableEntry<String, Handler<ErrorResult>>> by delegate, EventInterface<ErrorResult> {

    override val listenerCount get() = delegate.listeners.size
    override fun subscribe(handle: String?, callback: ErrorResult.() -> Unit) {
        if (handle == null) delegate += callback else delegate[handle] = callback
    }

    override fun unsubscribe(handle: String) {
        delegate -= handle
    }

    override fun emit(eventData: ErrorResult) = delegate(eventData)
}
