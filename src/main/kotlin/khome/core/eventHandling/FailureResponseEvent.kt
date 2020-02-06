package khome.core.eventHandling

import khome.core.Result

internal class FailureResponseEvent(delegate: Event<Result>) :
    Iterable<MutableMap.MutableEntry<String, Handler<Result>>> by delegate, EventInterface<Result> {
    val eventHandler = delegate
    override val listenerCount get() = eventHandler.listeners.size
    override fun subscribe(handle: String?, callback: suspend Result.() -> Unit) {
        if (handle == null) eventHandler += callback else eventHandler[handle] = callback
    }

    override fun unsubscribe(handle: String) {
        eventHandler -= handle
    }

    override suspend fun emit(eventData: Result) = eventHandler(eventData)
}
