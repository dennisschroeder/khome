package khome.calling

import khome.*
import khome.core.logger
import kotlinx.coroutines.*
import khome.core.serializer
import khome.core.MessageInterface
import khome.Khome.Companion.services
import khome.Khome.Companion.idCounter
import khome.core.entities.EntityInterface
import khome.Khome.Companion.callServiceContext
import khome.Khome.Companion.isSandBoxModeActive
import com.google.gson.annotations.SerializedName
import io.ktor.http.cio.websocket.WebSocketSession
import khome.calling.exceptions.DomainNotFoundException
import khome.calling.exceptions.ServiceNotFoundException

/**
 * A function to build an [ServiceCaller] object, which is the base
 * to all [home-assistant](https://www.home-assistant.io/) websocket api calls.
 * The ServiceCaller object is then serialized and send to the websocket api.
 * [Home-Assistant Websocket-Api](https://developers.home-assistant.io/docs/en/external_api_websocket.html).
 *
 * @see ServiceCaller
 */
@ObsoleteCoroutinesApi
fun WebSocketSession.callService(init: ServiceCaller.() -> Unit) {
    runBlocking {
        withContext(callServiceContext) {
            val callService = ServiceCaller(
                idCounter.incrementAndGet(),
                "call_service",
                null,
                null,
                null
            ).apply(init)

            when {
                isSandBoxModeActive -> {
                    val domain = callService.domain.toString().toLowerCase()
                    val service = callService.service.toString().toLowerCase()

                    when {
                        domain !in services -> throw DomainNotFoundException("$domain is not an registered domain in homeassistant")
                        service !in services[domain]!! -> throw ServiceNotFoundException("$service is not an available service under $domain in homeassistant")
                    }
                }
                else -> {
                    callWebSocketApi(callService.toJson())
                    logger.info { "Called Service with: " + callService.toJson() }
                }
            }
        }
    }
}

internal data class EntityId(override var entityId: String?) : ServiceDataInterface

internal data class EntityIds(
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
data class ServiceCaller(
    private var id: Int,
    private val type: String = "call_service",
    var domain: DomainInterface?,
    var service: ServiceInterface?,
    var serviceData: ServiceDataInterface?
) : MessageInterface {
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

