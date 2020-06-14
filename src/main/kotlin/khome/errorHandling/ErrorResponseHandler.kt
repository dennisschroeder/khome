package khome.errorHandling

import khome.core.ErrorResponse
import khome.events.EventHandler
import khome.observability.Switchable
import java.util.concurrent.atomic.AtomicBoolean

data class ErrorResponseData(val commandId: Int, val errorResponse: ErrorResponse)

internal class ErrorResponseHandlerImpl(
    private val f: (ErrorResponseData) -> Unit,
    override var enabled: AtomicBoolean = AtomicBoolean(true)
) : EventHandler<ErrorResponseData>, Switchable {
    override fun handle(eventData: ErrorResponseData) {
        f(eventData)
    }
}
