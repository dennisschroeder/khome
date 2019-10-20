package khome.core.eventHandling

import khome.core.ErrorResult
import khome.core.Result
import khome.core.dependencyInjection.internalRef

internal class FailureResponseEvents : Event<ErrorResult>(), EventInterface<ErrorResult> {

    override val stateListenerCount get() = this.listeners.size
    override fun subscribe(handle: String?, callback: ErrorResult.() -> Unit) {
        if (handle == null)
            this += callback
        else
            this[handle] = callback
    }

    override fun unsubscribe(handle: String) = this.minus(handle)

    override fun emit(eventData: ErrorResult) = this(eventData)
}
