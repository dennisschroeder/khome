package khome.errorHandling

typealias ExceptionHandlerFunction = (exception: Throwable) -> Unit

internal interface ExceptionHandler {
    fun handleExceptions(exception: Throwable)
}

internal class ObserverExceptionHandler(private val f: ExceptionHandlerFunction) : ExceptionHandler {
    override fun handleExceptions(exception: Throwable) {
        f(exception)
    }
}

internal class EventHandlerExceptionHandler(private val f: ExceptionHandlerFunction) : ExceptionHandler {
    override fun handleExceptions(exception: Throwable) {
        f(exception)
    }
}
