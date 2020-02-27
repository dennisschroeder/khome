package khome.core

import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent
import io.ktor.util.KtorExperimentalAPI
import khome.calling.ServiceDataInterface
import khome.core.clients.RestApiClient
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.eventHandling.HassEvent
import khome.core.mapping.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.async
import org.koin.core.get
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
abstract class KhomeComponent() : KhomeKoinComponent(), CoroutineScope {
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext

    val restClient: RestApiClient by inject()
    fun ServiceDataInterface.toJson(): String = get<ObjectMapper>().toJson(this)
    fun MessageInterface.toJson(): String = get<ObjectMapper>().toJson(this)

    inline fun <reified Event : HassEvent> emitEvent(payload: Any? = null) =
        async {
            val event = get<Event>()
            restClient.post<HttpResponse> {
                url { encodedPath = "/api/events/${event.eventType}" }
                body = payload?.let { it } ?: EmptyContent
            }
        }
}
