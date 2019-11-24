package khome.core.eventHandling

import khome.core.Result

internal class FailureResponseEvent(private val delegate: Event<Result>) :
    Iterable<MutableMap.MutableEntry<String, Handler<Result>>> by delegate, EventInterface<Result> {

    override val listenerCount get() = delegate.listeners.size
    override fun subscribe(handle: String?, callback: Result.() -> Unit) {
        if (handle == null) delegate += callback else delegate[handle] = callback
    }

    override fun unsubscribe(handle: String) {
        delegate -= handle
    }

    override fun emit(eventData: Result) = delegate(eventData)
}
