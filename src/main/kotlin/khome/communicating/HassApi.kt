package khome.communicating

import com.google.gson.annotations.SerializedName
import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.communicating.CommandType.CALL_SERVICE
import khome.communicating.CommandType.SUBSCRIBE_EVENTS
import khome.core.clients.RestApiClient
import khome.core.mapping.ObjectMapper
import khome.entities.EntityId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

internal class SubscribeEventCommand(private val eventType: String) : HassApiCommand {
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
    var domain: String? = null,
    val service: String,
    override var id: Int? = null,
    val serviceData: SD? = null,
    override val type: CommandType = CALL_SERVICE
) : HassApiCommand

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
internal class HassApi(
    private val khomeSession: KhomeSession,
    private val objectMapper: ObjectMapper,
    private val restApiClient: RestApiClient
) {
    private val logger = KotlinLogging.logger { }
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val serviceContext = ServiceCoroutineContext()

    @Synchronized
    fun sendHassApiCommand(command: HassApiCommand) =
        coroutineScope.launch(serviceContext) {
            command.id = CALLER_ID.incrementAndGet() // has to be called within single thread to prevent race conditions
            objectMapper.toJson(command).let { serializedCommand ->
                khomeSession.callWebSocketApi(serializedCommand)
                    .also { logger.info { "Called hass api with message: $serializedCommand" } }
            }
        }

    fun emitEvent(eventType: String, eventData: Any?) {
        coroutineScope.launch {
            restApiClient.post<HttpResponse> {
                url { encodedPath = "/api/events/$eventType" }
                body = eventData ?: EmptyContent
            }
        }
    }

    fun emitEventAsync(eventType: String, eventData: Any?) =
        coroutineScope.async {
            restApiClient.post<HttpResponse> {
                url { encodedPath = "/api/events/$eventType" }
                body = eventData ?: EmptyContent
            }
        }
}
