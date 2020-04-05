package khome.core

import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent
import io.ktor.util.KtorExperimentalAPI
import khome.calling.EntityBasedServiceCall
import khome.calling.ServiceDataInterface
import khome.calling.callService
import khome.core.clients.RestApiClient
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.entities.EntityInterface
import khome.core.eventHandling.HassEvent
import khome.core.mapping.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.withContext
import org.koin.core.get
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Suppress("unused")
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
abstract class KhomeComponent() : KhomeKoinComponent(), CoroutineScope {
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext

    val restClient: RestApiClient by inject()
    fun ServiceDataInterface.toJson(): String = get<ObjectMapper>().toJson(this)
    fun MessageInterface.toJson(): String = get<ObjectMapper>().toJson(this)

    suspend inline fun <reified Event : HassEvent> emitEvent(payload: Any? = null) =
        withContext(Dispatchers.Default) {
            val event = get<Event>()
            restClient.post<HttpResponse> {
                url { encodedPath = "/api/events/${event.eventType}" }
                body = payload?.let { it } ?: EmptyContent
            }
        }

    inline fun <reified CallType : EntityBasedServiceCall> Iterable<EntityInterface>.callServiceEach(noinline mutate: CallType.(EntityInterface) -> Unit) =
        forEach {
            callService<CallType> {
                mutate(it)
            }
        }
}
