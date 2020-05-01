package khome.core.events

import khome.core.StateChangedResponse
import kotlinx.coroutines.flow.collect

class StateChangeEvent(delegate: Event<StateChangedResponse>) : EventInterface<StateChangedResponse> {
    private val eventHandler = delegate

    override val listenerCount get() = eventHandler.listeners.size
    override fun subscribe(handle: String?, callback: (StateChangedResponse) -> Unit) {
        if (handle == null) eventHandler += callback else eventHandler[handle] = callback
    }

    operator fun get(handle: String) =
        eventHandler[handle]

    override fun unsubscribe(handle: String) {
        eventHandler -= handle
    }

    override suspend fun emit(eventData: StateChangedResponse) =
        eventHandler.collect { handlerFunction ->
            handlerFunction(eventData)
        }
}
