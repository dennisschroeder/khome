package khome.errorHandling

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

interface ExceptionHandler {
    fun handleExceptions(exception: Throwable)
}

internal class ObserverExceptionHandler(private val f: (Throwable) -> Unit) : ExceptionHandler {
    override fun handleExceptions(exception: Throwable) {
        f(exception)
    }
}

internal class AsyncObserverExceptionHandler(private val f: (Throwable) -> Unit): AbstractCoroutineContextElement(CoroutineExceptionHandler), CoroutineExceptionHandler {
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        f(exception)
    }
}
