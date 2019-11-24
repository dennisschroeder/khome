package khome.calling

import com.google.gson.annotations.SerializedName
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.core.MessageInterface
import khome.core.dependencyInjection.CallerID
import khome.core.dependencyInjection.KhomeComponent
import khome.core.dependencyInjection.ServiceCoroutineContext
import khome.core.logger
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.core.get
import org.koin.core.inject

/**
 * A function to build an [ServiceCaller] object, which is the base
 * to all [home-assistant](https://www.home-assistant.io/) websocket api calls.
 * The ServiceCaller object is then serialized and send to the websocket api.
 * [Home-Assistant Websocket-Api](https://developers.home-assistant.io/docs/en/external_api_websocket.html).
 *
 * @see ServiceCaller
 */
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
inline fun <reified CallType : ServiceCaller> KhomeSession.callService() {
    val servicePayload: CallType by inject()
    val serviceCoroutineContext: ServiceCoroutineContext by inject()
    launch(serviceCoroutineContext) {
        servicePayload.id = get<CallerID>().incrementAndGet()
        callWebSocketApi(servicePayload.toJson())
        logger.info { "Called Service with: " + servicePayload.toJson() }
    }
}

data class EntityId(override var entityId: String?) : ServiceDataInterface

data class EntityIds(
    @SerializedName("entity_id") var entityIds: String,
    @Transient override var entityId: String?
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
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
abstract class ServiceCaller : KhomeComponent(), MessageInterface {
    var id: Int = 0
    private val type: String = "call_service"
    abstract var domain: DomainInterface
    abstract var service: ServiceInterface
    abstract var serviceData: ServiceDataInterface
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
}

/**
 * Domains that are supported from Khome
 */
enum class Domain : DomainInterface {
    COVER, LIGHT, HOME_ASSISTANT, MEDIA_PLAYER, NOTIFY, PERSISTENT_NOTIFICATION
}
