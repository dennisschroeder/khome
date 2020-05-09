package khome.calling

import io.ktor.util.KtorExperimentalAPI
import khome.calling.errors.DomainNotFoundException
import khome.calling.errors.ServiceNotFoundException
import khome.core.ServiceCallInterface
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.entities.EntityId
import khome.core.entities.EntitySubject
import khome.core.servicestore.ServiceStoreInterface
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get

internal typealias ServiceCallMutator<T> = T.() -> Unit

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
abstract class EntityIdOnlyServiceCall(
    domain: DomainInterface,
    service: ServiceInterface
) : EntityBasedServiceCall(domain, service) {

    inline fun <reified Entity : EntitySubject<*>> entity() =
        entity(get<Entity>())

    fun entity(entity: EntitySubject<*>) {
        serviceData.apply {
            entityId = entity.entityId
        }
    }

    override val serviceData: EntityBasedServiceDataInterface =
        EntityIdOnlyServiceData()
}

class EntityIdOnlyServiceData : EntityBasedServiceDataInterface {
    override var entityId: EntityId? = EntityId("", "")
}

/**
 * The base class to build the payload for home-assistant websocket api calls.
 * @see callService
 *
 * @property domain One of the from Khome supported domains [Domain].
 * @property service One of the services that are available for the given [domain].
 */
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
abstract class ServiceCall(
    val domain: DomainInterface,
    val service: ServiceInterface
) : KhomeKoinComponent, ServiceCallInterface {
    override var id: Int = 0
    private val type: String = "call_service"

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
            throw ServiceNotFoundException("Service: \"${_service}service\" not found under domain: \"${_domain}\"in homeassistant Services")
    }
}

abstract class EntityBasedServiceCall(
    domain: DomainInterface,
    service: ServiceInterface
) : ServiceCall(domain, service) {
    abstract val serviceData: EntityBasedServiceDataInterface
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

abstract class EntityBasedServiceData : EntityBasedServiceDataInterface {
    override var entityId: EntityId? = null
}

interface EntityBasedServiceDataInterface {
    var entityId: EntityId?
}

/**
 * Domains that are supported from Khome
 */
enum class Domain : DomainInterface {
    COVER, LIGHT, HOMEASSISTANT, MEDIA_PLAYER, NOTIFY, PERSISTENT_NOTIFICATION
}
