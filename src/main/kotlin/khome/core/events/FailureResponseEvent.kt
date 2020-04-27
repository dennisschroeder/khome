package khome.core.events

import khome.core.ResultResponse
import kotlinx.coroutines.CoroutineScope

internal class FailureResponseEvent(delegate: Event<ResultResponse>) :
    Iterable<MutableMap.MutableEntry<String, Handler<ResultResponse>>> by delegate, EventInterface<ResultResponse> {
    val eventHandler = delegate
    override val listenerCount get() = eventHandler.listeners.size
    override fun subscribe(handle: String?, callback: suspend CoroutineScope.(ResultResponse) -> Unit) {
        if (handle == null) eventHandler += callback else eventHandler[handle] = callback
    }

    override fun unsubscribe(handle: String) {
        eventHandler -= handle
    }

    override suspend fun emit(eventData: ResultResponse) = eventHandler(eventData)
}
