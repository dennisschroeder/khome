package khome.core.events

import khome.core.StateChangedResponse
import kotlinx.coroutines.CoroutineScope

class StateChangeEvent(delegate: Event<StateChangedResponse>) :  EventInterface<StateChangedResponse> {
    private val eventHandler = delegate

    override val listenerCount get() = eventHandler.listeners.size
    override fun subscribe(handle: String?, callback: suspend CoroutineScope.(StateChangedResponse) -> Unit) {
        if (handle == null) eventHandler += callback else eventHandler[handle] = callback
    }

    override fun unsubscribe(handle: String) {
        eventHandler -= handle
    }

    override suspend fun emit(eventData: StateChangedResponse) = eventHandler(eventData)
}
