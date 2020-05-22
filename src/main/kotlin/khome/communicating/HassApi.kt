package khome.communicating

import com.google.gson.annotations.SerializedName
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.calling.ServiceCoroutineContext
import khome.core.clients.RestApiClient
import khome.core.mapping.ObjectMapper
import khome.entities.EntityId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger

internal val CALLER_ID = AtomicInteger(0)

internal enum class CommandType {
    @SerializedName("call_service") CALL_SERVICE,
    @SerializedName("subscribe_events") SUBSCRIBE_EVENTS,
    @SerializedName("get_services") GET_SERVICES,
    @SerializedName("get_states") GET_STATES
}

interface CommandDataWithEntityId {
    val entityId: EntityId
}

internal interface HassApiCommand {
    val type: CommandType
    var id: Int?
}

internal data class EntityIdOnlyServiceData(override val entityId: EntityId) : CommandDataWithEntityId

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
internal data class HassApiCommandImpl<SD>(
    val domain: String,
    val service: String,
    override val type: CommandType = CommandType.CALL_SERVICE,
    override var id: Int? = null,
    val serviceData: SD? = null
) : HassApiCommand

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
internal class HassApi(
    private val khomeSession: KhomeSession,
    private val objectMapper: ObjectMapper,
    private val restApiClient: RestApiClient
) : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val logger = KotlinLogging.logger { }

    fun sendHassApiCommand(command: HassApiCommand) =
        launch(ServiceCoroutineContext()) {
            command.id = CALLER_ID.incrementAndGet() // has to be called within single thread to prevent race conditions
            objectMapper.toJson(command).let { serializedRequest ->
                khomeSession.callWebSocketApi(serializedRequest)
                    .also { logger.info { "Called hass api with message: $serializedRequest" } }
            }
        }
}
