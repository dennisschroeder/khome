package khome.core.events

import khome.core.ResultResponse
import kotlinx.coroutines.flow.collect

class ErrorResponseEvent(delegate: Event<ResultResponse>) :
    Iterable<MutableMap.MutableEntry<String, Handler<ResultResponse>>> by delegate, EventInterface<ResultResponse> {
    private val eventHandler = delegate
    override val listenerCount get() = eventHandler.listeners.size
    override fun subscribe(handle: String?, callback: (ResultResponse) -> Unit) {
        if (handle == null) eventHandler += callback else eventHandler[handle] = callback
    }

    override fun unsubscribe(handle: String) {
        eventHandler -= handle
    }

    override suspend fun emit(eventData: ResultResponse) =
        eventHandler.collect { handlerFunction ->
            handlerFunction(eventData)
        }
}
