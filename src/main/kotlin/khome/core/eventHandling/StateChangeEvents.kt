package khome.core.eventHandling

import khome.core.EventResult

internal class StateChangeEvents : Event<EventResult>(), EventInterface<EventResult> {
    override val stateListenerCount get() = this.listeners.size
    override fun subscribe(handle: String?, callback: EventResult.() -> Unit) {
        if (handle == null)
            this += callback
        else
            this[handle] = callback
    }

    override fun unsubscribe(handle: String) = this.minus(handle)

    override fun emit(eventData: EventResult) = this(eventData)
}
