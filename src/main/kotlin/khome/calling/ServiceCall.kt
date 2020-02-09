package khome.calling

import com.google.gson.annotations.SerializedName
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.calling.errors.DomainNotFoundException
import khome.calling.errors.ServiceNotFoundException
import khome.core.KhomeComponent
import khome.core.MessageInterface
import khome.core.ServiceStoreInterface
import khome.core.dependencyInjection.CallerID
import khome.core.dependencyInjection.KhomeKoinComponent
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.core.get

internal typealias ServiceCallMutator<T> = T.() -> Unit

/**
 * A function to build an [ServiceCall] object, which is the base
 * to all [home-assistant](https://www.home-assistant.io/) websocket api calls.
 * The ServiceCaller object is then serialized and send to the websocket api.
 * [Home-Assistant Websocket-Api](https://developers.home-assistant.io/docs/en/external_api_websocket.html).
 *
 * @see ServiceCall
 */
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
inline fun <reified CallType : ServiceCall> KhomeComponent.callService(noinline mutate: ServiceCallMutator<CallType>? = null) {
    val servicePayload: CallType = get()
    val serviceCoroutineContext: ServiceCoroutineContext = get()
    servicePayload.id = get<CallerID>().incrementAndGet()
    if (mutate != null) servicePayload.apply(mutate)
    val session = get<KhomeSession>()
    session.launch(serviceCoroutineContext) {
        session.callWebSocketApi(servicePayload.toJson())
        session.logger.info { "Called Service with: " + servicePayload.toJson() }
    }
}

data class EntityId(var entityId: String?) : ServiceDataInterface

data class EntityIds(
    @SerializedName("entity_id") var entityIds: String
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
abstract class ServiceCall(
    val domain: DomainInterface,
    val service: ServiceInterface
) : KhomeKoinComponent(), MessageInterface {
    var id: Int = 0
    private val type: String = "call_service"
    abstract val serviceData: ServiceDataInterface?
    @Transient
    private val serviceStore: ServiceStoreInterface = get()
    @Transient
    private val _domain = domain.toString().toLowerCase()
    @Transient
    private val _service = service.toString().toLowerCase()

    init {
        if (_domain !in serviceStore)
            throw DomainNotFoundException("ServiceDomain: \"$_domain\" not found in homeassistant Services")
        if (!serviceStore[_domain]!!.contains(_service))
            throw ServiceNotFoundException("Service: \"${_service}service\" not found under domain: \"${_domain}domain\"in homeassistant Services")
    }
}

/**
 * Main entry point to create new domain enum classes
 */
interface DomainInterface

/**
 * Main entry point to create new service enum classes
 */
interface ServiceInterface

/**
 * Main entry point to create own service data classes
 */
interface ServiceDataInterface

/**
 * Domains that are supported from Khome
 */
enum class Domain : DomainInterface {
    COVER, LIGHT, HOMEASSISTANT, MEDIA_PLAYER, NOTIFY, PERSISTENT_NOTIFICATION
}
