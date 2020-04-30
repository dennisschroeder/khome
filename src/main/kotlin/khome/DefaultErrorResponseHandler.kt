package khome

import io.ktor.util.KtorExperimentalAPI
import khome.calling.PersistentNotificationCreate
import khome.calling.callService
import khome.core.ConfigurationInterface
import khome.core.KhomeComponent
import khome.core.ResultResponse
import khome.core.onErrorResponse
import khome.listening.LifeCycleHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi

@Suppress("FunctionName")
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class DefaultErrorResponseHandler(
    private val configuration: ConfigurationInterface
) : KhomeComponent(), ErrorResponseHandlerInterface {

    init {
        `create persistent notification when homeassistant responses with error`()
    }

    private fun `create persistent notification when homeassistant responses with error`() =
        onError { errorResponse ->
            callService<PersistentNotificationCreate> {
                configure {
                    notificationId = "error_response_${errorResponse.id}"
                    title = "Error in Khome application: ${configuration.name}"
                    message = """
                        Homeassistant responded with message: 
                        **${errorResponse.error?.code} : ${errorResponse.error?.message}**
                        in **error response** with caller id: **${errorResponse.id}**
                    """.trimIndent()
                }
            }
        }

    override fun onError(callback: suspend CoroutineScope.(ResultResponse) -> Unit): LifeCycleHandler =
        onErrorResponse(callback = callback)
}

interface ErrorResponseHandlerInterface {
    fun onError(callback: suspend CoroutineScope.(ResultResponse) -> Unit): LifeCycleHandler
}
