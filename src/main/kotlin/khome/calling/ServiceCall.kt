package khome.calling

import io.ktor.util.KtorExperimentalAPI
import khome.core.ServiceCallInterface
import khome.core.koin.KhomeComponent
import kotlinx.coroutines.ObsoleteCoroutinesApi

internal typealias ServiceCallMutator<T> = T.() -> Unit

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
abstract class EntityIdOnlyServiceCall(
    domain: Domain,
    service: ServiceInterface
) : EntityBasedServiceCall(domain, service) {

    fun entityId(id: String) {
        serviceData.apply {
            entityId = id
        }
    }

    override val serviceData: EntityBasedServiceDataInterface =
        EntityIdOnlyServiceData()
}

class EntityIdOnlyServiceData : EntityBasedServiceDataInterface {
    override var entityId: String? = ""
}

/**
 * The base class to build the payload for home-assistant websocket api calls.
 * @see callService
 *
 * @property domain One of the from Khome supported domains [HassDomain].
 * @property service One of the services that are available for the given [domain].
 */
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
abstract class ServiceCall(
    val domain: Domain,
    val service: ServiceInterface
) : KhomeComponent, ServiceCallInterface {
    override var id: Int = 0
    private val type: String = "call_service"
}

abstract class EntityBasedServiceCall(
    domain: Domain,
    service: ServiceInterface
) : ServiceCall(domain, service) {
    abstract val serviceData: EntityBasedServiceDataInterface
}

/**
 * Main entry point to create new domain enum classes
 */
interface Domain

/**
 * Main entry point to create new service enum classes
 */
interface ServiceInterface

/**
 * Main entry point to create own service data classes
 */
interface ServiceDataInterface

abstract class EntityBasedServiceData : EntityBasedServiceDataInterface {
    override var entityId: String? = null
}

interface EntityBasedServiceDataInterface {
    var entityId: String?
}

/**
 * Domains that are supported from Khome
 */
enum class HassDomain : Domain {
    SENSOR, SUN, COVER, LIGHT, HOMEASSISTANT, MEDIA_PLAYER, NOTIFY, PERSISTENT_NOTIFICATION, INPUT_BOOLEAN, INPUT_NUMBER
}
