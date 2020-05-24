package khome

import io.ktor.util.KtorExperimentalAPI
import khome.communicating.CommandDataWithEntityId
import khome.communicating.HassApi
import khome.communicating.HassApiCommandImpl
import khome.communicating.ServiceTypeResolver
import khome.core.State
import khome.core.boot.EventResponseConsumer
import khome.core.boot.KhomeModulesInitializer
import khome.core.boot.StateChangeEventSubscriber
import khome.core.boot.authentication.Authenticator
import khome.core.boot.servicestore.ServiceStoreInitializer
import khome.core.boot.statehandling.EntityStateInitializer
import khome.core.koin.KhomeComponent
import khome.entities.ActuatorStateUpdater
import khome.entities.EntityId
import khome.entities.SensorStateUpdater
import khome.entities.devices.Actuator
import khome.entities.devices.ActuatorImpl
import khome.entities.devices.Sensor
import khome.entities.devices.SensorImpl
import khome.observability.AsyncObserver
import khome.observability.Observer
import khome.observability.ObserverImpl
import khome.observability.WithHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import org.koin.core.get
import org.koin.core.inject
import kotlin.reflect.KClass

internal typealias SensorsByApiName = MutableMap<EntityId, SensorImpl<*>>
internal typealias ActuatorsByApiName = MutableMap<EntityId, ActuatorImpl<*>>
internal typealias ActuatorsByEntity = MutableMap<ActuatorImpl<*>, EntityId>
internal typealias ServiceTypeResolverByDomain = MutableMap<String, ServiceTypeResolver<*>>

interface KhomeApplication {
    suspend fun run()
    fun <S> createSensor(id: EntityId, stateValueType: KClass<*>): Sensor<S>
    fun <S> createActuator(id: EntityId, stateValueType: KClass<*>): Actuator<S>
    fun <S> createObserver(f: (WithHistory<State<S>>) -> Unit): Observer<State<S>>
    fun <S> createAsyncObserver(f: suspend CoroutineScope.(snapshot: WithHistory<State<S>>) -> Unit): Observer<State<S>>
    fun <S> registerServiceTypeMapper(domain: String, resolver: ServiceTypeResolver<S>)
}

@OptIn(
    ExperimentalStdlibApi::class,
    KtorExperimentalAPI::class,
    ObsoleteCoroutinesApi::class,
    ExperimentalCoroutinesApi::class
)
internal class KhomeApplicationImpl : KhomeApplication {
    private val logger = KotlinLogging.logger { }
    private val koin = object : KhomeComponent {}
    private val hassClient: HassClient by koin.inject()
    private val hassApi: HassApi by koin.inject()

    private val sensorsByApiName: SensorsByApiName = mutableMapOf()
    private val actuatorsByApiName: ActuatorsByApiName = mutableMapOf()
    private val actuatorsByEntity: ActuatorsByEntity = mutableMapOf()

    private val serviceTypeResolverByDomain: ServiceTypeResolverByDomain = mutableMapOf()

    override fun <S> createSensor(id: EntityId, stateValueType: KClass<*>): Sensor<S> =
        SensorImpl<S>(stateValueType).also { registerSensor(id, it) }

    override fun <S> createActuator(id: EntityId, stateValueType: KClass<*>): Actuator<S> =
        ActuatorImpl<S>(this, getServiceTypeResolver(id.domain), stateValueType).also { registerActuator(id, it) }

    override fun <S> createObserver(f: (WithHistory<State<S>>) -> Unit): Observer<State<S>> =
        ObserverImpl(f)

    override fun <S> createAsyncObserver(f: suspend CoroutineScope.(snapshot: WithHistory<State<S>>) -> Unit): Observer<State<S>> =
        AsyncObserver(f)

    @Suppress("UNCHECKED_CAST")
    override fun <S> registerServiceTypeMapper(domain: String, resolver: ServiceTypeResolver<S>) {
        serviceTypeResolverByDomain[domain] = resolver as ServiceTypeResolver<*>
    }

    private fun getServiceTypeResolver(domain: String) = serviceTypeResolverByDomain[domain]
        ?: throw RuntimeException("No service type resolver found for $domain. Please register one.")

    private fun registerSensor(entityId: EntityId, sensor: SensorImpl<*>) {
        sensorsByApiName[entityId] = sensor
        logger.info { "Registered sensor with id: $entityId" }
    }

    private fun registerActuator(entityId: EntityId, actuator: ActuatorImpl<*>) {
        actuatorsByApiName[entityId] = actuator
        actuatorsByEntity[actuator] = entityId
        logger.info { "Registered actuator with id: $entityId" }
    }

    internal fun <State> enqueueStateChange(
        actuator: ActuatorImpl<State>,
        commandImpl: HassApiCommandImpl<CommandDataWithEntityId>
    ) {
        val entityId = actuatorsByEntity[actuator] ?: throw RuntimeException("Entity not registered: $actuator")
        commandImpl.apply {
            domain = entityId.domain
            serviceData?.entityId = entityId
        }
        hassApi.sendHassApiCommand(commandImpl)
    }

    override suspend fun run() =
        hassClient.startSession {

            Authenticator(
                khomeSession = this,
                configuration = get()
            ).runBootSequence()

            ServiceStoreInitializer(
                khomeSession = this,
                serviceStore = get()
            ).runBootSequence()

            KhomeModulesInitializer(khomeSession = this).runBootSequence()

            EntityStateInitializer(
                khomeSession = this,
                sensorStateUpdater = SensorStateUpdater(sensorsByApiName),
                actuatorStateUpdater = ActuatorStateUpdater(actuatorsByApiName)
            ).runBootSequence()

            StateChangeEventSubscriber(khomeSession = this).runBootSequence()

            EventResponseConsumer(
                khomeSession = this,
                objectMapper = get(),
                sensorStateUpdater = SensorStateUpdater(sensorsByApiName),
                actuatorStateUpdater = ActuatorStateUpdater(actuatorsByApiName)
            ).runBootSequence()
        }
}
