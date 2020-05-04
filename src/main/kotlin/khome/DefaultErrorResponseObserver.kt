package khome

import io.ktor.util.KtorExperimentalAPI
import khome.calling.PersistentNotificationCreate
import khome.core.ConfigurationInterface
import khome.core.ResultResponse
import khome.listening.LifeCycleHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get

@Suppress("FunctionName")
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class DefaultErrorResponseObserver(
    private val configuration: ConfigurationInterface,
    private val hassApi: HassApi
) : ErrorResponseObserver(), ErrorResponseHandlerInterface {

    init {
        `create persistent notification when homeassistant responses with error`()
    }

    private fun `create persistent notification when homeassistant responses with error`() =
        onError { errorResponse ->
            val serviceCall: PersistentNotificationCreate = get()
            serviceCall.apply {
                configure {
                    notificationId = "error_response_${errorResponse.id}"
                    title = "Error in Khome application: ${configuration.name}"
                    message = """
                        Homeassistant responded with message: 
                        **${errorResponse.error?.code} : ${errorResponse.error?.message}**
                        in **error response** with caller id: **${errorResponse.id}**
                    """.trimIndent()
                }
                hassApi.callHassService(serviceCall)
            }
        }

    override fun onError(callback: suspend CoroutineScope.(ResultResponse) -> Unit): LifeCycleHandler =
        onErrorResponse(callback = callback)
}

interface ErrorResponseHandlerInterface {
    fun onError(callback: suspend CoroutineScope.(ResultResponse) -> Unit): LifeCycleHandler
}
