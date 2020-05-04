package khome

import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent
import khome.calling.ServiceCall
import khome.calling.ServiceCoroutineContext
import khome.core.clients.RestApiClient
import khome.core.dependencyInjection.CallerID
import khome.core.events.EventData
import khome.core.events.HassEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HassApi @PublishedApi internal constructor(
    private val khomeSession: KhomeSession,
    private val callerID: CallerID,
    private val serviceCoroutineContext: ServiceCoroutineContext,
    private val restApiClient: RestApiClient
) {

    suspend fun callHassService(service: ServiceCall) =
        withContext(serviceCoroutineContext) {
            service.id = callerID.incrementAndGet()
            khomeSession.callWebSocketApi(service)
        }

    suspend fun emitHassEvent(event: HassEvent, eventData: EventData? = null): HttpResponse =
        withContext(Dispatchers.IO) {
            restApiClient.post<HttpResponse> {
                url { encodedPath = "/api/events/${event.eventType}" }
                body = eventData ?: EmptyContent
            }
        }
}
