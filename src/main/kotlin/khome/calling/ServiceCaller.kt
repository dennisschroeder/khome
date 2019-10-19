package khome.calling

import khome.*
import khome.core.logger
import kotlinx.coroutines.*
import khome.core.serializer
import khome.core.MessageInterface
import khome.Khome.Companion.idCounter
import khome.core.entities.EntityInterface
import khome.Khome.Companion.callServiceContext
import com.google.gson.annotations.SerializedName
import io.ktor.http.cio.websocket.WebSocketSession
import khome.Khome.Companion.services
import khome.calling.Exceptions.DomainNotFoundException
import khome.core.dependencyInjection.KhomeKoinComponent
import javax.management.ServiceNotFoundException

/**
 * A function to build an [ServiceCaller] object, which is the base
 * to all [home-assistant](https://www.home-assistant.io/) websocket api calls.
 * The ServiceCaller object is then serialized and send to the websocket api.
 * [Home-Assistant Websocket-Api](https://developers.home-assistant.io/docs/en/external_api_websocket.html).
 *
 * @see ServiceCaller
 */
@ObsoleteCoroutinesApi
fun WebSocketSession.callService(payload: ServiceCaller) = launch(callServiceContext) {
    payload.id = idCounter.incrementAndGet()
    callWebSocketApi(payload.toJson())
    logger.info { "Called Service with: " + payload.toJson() }
}


open class EntityId(override var entityId: String?) : ServiceDataInterface

data class EntityIds(
    @SerializedName("entity_id") var entityIds: String,
    override var entityId: String?
) : ServiceDataInterface

/**
 * The base class to build the payload for home-assistant websocket api calls.
 * @see callService
 * @see ServiceDataInterface
 *
 * @property domain One of the from Khome supported domains [Domain].
 * @property service One of the services that are available for the given [domain].
 * @property serviceData ServiceData object to send context data that fits to the given [service].
 */
abstract class ServiceCaller : KhomeKoinComponent(), MessageInterface {
    var id: Int = 0
    private val type: String = "call_service"
    abstract var domain: DomainInterface
    abstract var service: ServiceInterface
    abstract var serviceData: ServiceDataInterface

    /**
     * Some services only need an entity id as context data.
     * This function serves the needs for that.
     */
    fun entityId(entity: EntityInterface) {
        serviceData = EntityId(entity.id)
    }
}

/**
 * Main entry point to create own domain enum classes
 */
interface DomainInterface

/**
 * Main entry point to create own service enum classes
 */
interface ServiceInterface

/**
 * Main entry point to create own service data classes
 */
interface ServiceDataInterface {
    var entityId: String?
    fun toJson(): String = serializer.toJson(this)
}

/**
 * Domains that are supported from Khome
 */
enum class Domain : DomainInterface {
    COVER, LIGHT, HOMEASSISTANT, MEDIA_PLAYER, NOTIFY, PERSISTENT_NOTIFICATION
}

