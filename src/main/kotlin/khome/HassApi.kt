package khome

import khome.calling.ServiceCall
import khome.calling.ServiceCoroutineContext
import khome.core.clients.RestApiClient
import khome.core.koin.CallerID
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
}
