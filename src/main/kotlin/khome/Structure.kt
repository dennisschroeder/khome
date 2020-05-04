package khome

import io.ktor.util.KtorExperimentalAPI
import khome.calling.EntityBasedServiceCall
import khome.calling.ServiceCall
import khome.calling.ServiceCallMutator
import khome.calling.ServiceDataInterface
import khome.core.ErrorResponseListener
import khome.core.MessageInterface
import khome.core.ResultResponse
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.entities.EntityCollection
import khome.core.entities.EntityInterface
import khome.core.events.EventData
import khome.core.events.HassEvent
import khome.core.mapping.ObjectMapper
import khome.listening.HassEventListener
import khome.listening.StateListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import org.koin.core.get

@Suppress("unused")
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
abstract class KhomeComponent : KhomeKoinComponent {
    open val logger = KotlinLogging.logger { }

    val hassApi: HassApi = get()
    private val objectMapper = get<ObjectMapper>()
    fun ServiceDataInterface.toJson(): String = objectMapper.toJson(this)
    fun MessageInterface.toJson(): String = objectMapper.toJson(this)

    /**
     * A function to build an [ServiceCall] object, which is the base
     * to all [home-assistant](https://www.home-assistant.io/) websocket service api calls.
     * The ServiceCaller object is then serialized and send to homeassistant.
     * [Home-Assistant Websocket-Api](https://developers.home-assistant.io/docs/en/external_api_websocket.html).
     *
     * @see ServiceCall
     */
    @ObsoleteCoroutinesApi
    @KtorExperimentalAPI
    suspend inline fun <reified CallType : ServiceCall> callService(noinline mutate: ServiceCallMutator<CallType>? = null) {
        val service: CallType = get()
        if (mutate != null) service.apply(mutate)
        hassApi.callHassService(service)
    }

    suspend inline fun <reified Event : HassEvent> emitEvent(eventData: EventData? = null) =
        hassApi.emitHassEvent(get<Event>(), eventData)

    suspend inline fun <reified CallType : EntityBasedServiceCall> Iterable<EntityInterface>.callServiceEach(noinline mutate: CallType.(EntityInterface) -> Unit) =
        forEach { callService<CallType> { mutate(it) } }
}

abstract class Repository<Entity : EntityInterface> : KhomeKoinComponent {
    abstract val entity: Entity

    @PublishedApi
    internal val hassApi = get<HassApi>()

    suspend inline fun <reified CallType : EntityBasedServiceCall> callService(noinline mutate: ServiceCallMutator<CallType>? = null) {
        val service = get<CallType>()
        if (mutate != null) service.apply(mutate)
        service.serviceData.entityId = entity
        hassApi.callHassService(service)
    }
}

abstract class CollectionRepository<EntityType : EntityInterface> : KhomeKoinComponent {
    abstract val entities: EntityCollection<EntityType>

    @PublishedApi
    internal val hassApi = get<HassApi>()

    suspend inline fun <reified CallType : EntityBasedServiceCall> callServiceEach(noinline mutate: ServiceCallMutator<CallType>? = null) =
        entities.forEach { entity ->
            val service = get<CallType>()
            if (mutate != null) service.apply(mutate)
            service.serviceData.entityId = entity
            hassApi.callHassService(service)
        }
}

abstract class EntityObserver<Entity : EntityInterface> : KhomeKoinComponent {

    abstract val observedEntity: Entity

    fun onStateChange(callback: suspend CoroutineScope.(Entity) -> Unit) =
        StateListener(
            context = Dispatchers.IO,
            entity = observedEntity,
            exceptionHandler = get(),
            stateChangeEvent = get(),
            listener = callback
        ).lifeCycleHandler
}

abstract class ErrorResponseObserver : KhomeKoinComponent {
    fun onErrorResponse(callback: suspend CoroutineScope.(ResultResponse) -> Unit) =
        ErrorResponseListener(
            context = Dispatchers.IO,
            errorResponseEvent = get(),
            exceptionHandler = get(),
            listener = callback
        ).lifeCycleHandler
}

abstract class HassEventObserver<EventType : HassEvent> : KhomeKoinComponent {

    abstract val observedHassEvent: EventType

    fun onHassEvent(callback: suspend CoroutineScope.(EventData) -> Unit) =
        HassEventListener(
            context = Dispatchers.IO,
            hassEvent = observedHassEvent,
            exceptionHandler = get(),
            listener = callback
        ).lifeCycleHandler
}
