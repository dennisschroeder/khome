package khome.errorHandling

import khome.core.ErrorResponse
import khome.events.EventHandler
import khome.events.SwitchableEventHandler
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A data class storing error response meta data
 *
 * @property commandId the command id refers to the same id that was used by the command that causes the error response
 * @property errorResponse see [ErrorResponse]
 *
 */
data class ErrorResponseData(val commandId: Int, val errorResponse: ErrorResponse)

internal class ErrorResponseHandlerImpl(
    private val f: (ErrorResponseData) -> Unit,
    override val enabled: AtomicBoolean = AtomicBoolean(true)
) : EventHandler<ErrorResponseData>, SwitchableEventHandler<ErrorResponseData> {
    override fun handle(eventData: ErrorResponseData) {
        f(eventData)
    }
}
