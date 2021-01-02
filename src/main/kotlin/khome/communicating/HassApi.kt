package khome.communicating

import com.google.gson.annotations.SerializedName
import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.communicating.CommandType.CALL_SERVICE
import khome.communicating.CommandType.SUBSCRIBE_EVENTS
import khome.core.KhomeDispatchers
import khome.core.clients.RestApiClient
import khome.core.mapping.ObjectMapperInterface
import khome.values.Domain
import khome.values.EntityId
import khome.values.EventType
import khome.values.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

internal val CALLER_ID = AtomicInteger(0)

internal enum class CommandType {
    @SerializedName("call_service")
    CALL_SERVICE,

    @SerializedName("subscribe_events")
    SUBSCRIBE_EVENTS,

    @SerializedName("get_services")
    GET_SERVICES,

    @SerializedName("get_states")
    GET_STATES
}

interface CommandDataWithEntityId {
    var entityId: EntityId
}

internal interface HassApiCommand {
    val type: CommandType
    var id: Int?
}

internal class SubscribeEventCommand(private val eventType: EventType) : HassApiCommand {
    override val type: CommandType = SUBSCRIBE_EVENTS
    override var id: Int? = null
}

abstract class DesiredServiceData : CommandDataWithEntityId {
    override lateinit var entityId: EntityId
}

class EntityIdOnlyServiceData : DesiredServiceData()

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
internal data class ServiceCommandImpl<SD>(
    var domain: Domain? = null,
    val service: Service,
    override var id: Int? = null,
    val serviceData: SD? = null,
    override val type: CommandType = CALL_SERVICE
) : HassApiCommand

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
internal class HassApiClientImpl(
    private val khomeSession: KhomeSession,
    private val objectMapper: ObjectMapperInterface,
    private val restApiClient: RestApiClient
) : HassApiClient {
    private val logger = KotlinLogging.logger { }
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun sendCommand(command: HassApiCommand) =
        coroutineScope.launch(KhomeDispatchers.CommandDispatcher) {
            command.id = CALLER_ID.incrementAndGet() // has to be called within single thread to prevent race conditions
            objectMapper.toJson(command).let { serializedCommand ->
                khomeSession.callWebSocketApi(serializedCommand)
                    .also { logger.info { "Called hass api with message: $serializedCommand" } }
            }
        }

    override fun emitEvent(eventType: String, eventData: Any?) {
        coroutineScope.launch {
            restApiClient.post<HttpResponse> {
                url { encodedPath = "/api/events/$eventType" }
                body = eventData ?: EmptyContent
            }
        }
    }

    override fun emitEventAsync(eventType: String, eventData: Any?) =
        coroutineScope.async {
            restApiClient.post<HttpResponse> {
                url { encodedPath = "/api/events/$eventType" }
                body = eventData ?: EmptyContent
            }
        }
}

internal interface HassApiClient {
    fun sendCommand(command: HassApiCommand): Job
    fun emitEvent(eventType: String, eventData: Any?)
    fun emitEventAsync(eventType: String, eventData: Any?): Deferred<HttpResponse>
}
