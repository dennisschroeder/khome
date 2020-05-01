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
import khome.core.events.HassEvent
import khome.core.mapping.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.koin.core.get
import org.koin.core.inject

@Suppress("unused")
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
abstract class KhomeComponent() : KhomeKoinComponent() {
    open val logger = KotlinLogging.logger { }

    val restClient: RestApiClient by inject()
    fun ServiceDataInterface.toJson(): String = get<ObjectMapper>().toJson(this)
    fun MessageInterface.toJson(): String = get<ObjectMapper>().toJson(this)

    suspend inline fun <reified Event : HassEvent> emitEvent(payload: Any? = null) =
        withContext(Dispatchers.IO) {
            val event = get<Event>()
            restClient.post<HttpResponse> {
                url { encodedPath = "/api/events/${event.eventType}" }
                body = payload ?: EmptyContent
            }
        }

    fun createEntity(entityId: String, payload: Any? = null) =
        CoroutineScope(Dispatchers.IO).launch {
            restClient.post<HttpResponse> {
                url { encodedPath = "/api/states/$entityId" }
                body = payload ?: EmptyContent
            }
        }

    inline fun <reified CallType : EntityBasedServiceCall> Iterable<EntityInterface>.callServiceEach(noinline mutate: CallType.(EntityInterface) -> Unit) =
        forEach {
            callService<CallType> {
                mutate(it)
            }
        }
}
