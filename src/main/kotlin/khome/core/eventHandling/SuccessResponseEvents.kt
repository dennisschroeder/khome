package khome.core.eventHandling

import khome.core.Result

internal class SuccessResponseEvents : Event<Result>(), EventInterface<Result> {

    override val stateListenerCount get() = this.listeners.size
    override fun subscribe(handle: String?, callback: Result.() -> Unit) {
        if (handle == null)
            this += callback
        else
            this[handle] = callback
    }

    override fun unsubscribe(handle: String) = this.minus(handle)

    override fun emit(eventData: Result) = this(eventData)
}
