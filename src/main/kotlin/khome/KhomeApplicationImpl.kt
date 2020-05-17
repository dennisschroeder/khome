package khome

import io.ktor.util.KtorExperimentalAPI
import khome.core.State
import khome.core.boot.EventResponseConsumer
import khome.core.boot.KhomeModulesInitializer
import khome.core.boot.StateChangeEventSubscriber
import khome.core.boot.authentication.Authenticator
import khome.core.boot.servicestore.ServiceStoreInitializer
import khome.core.boot.statehandling.EntityStateInitializer
import khome.core.koin.KhomeKoinComponent
import khome.entities.ActuatorStateUpdater
import khome.entities.EntityId
import khome.entities.SensorStateUpdater
import khome.entities.devices.Actuator
import khome.entities.devices.ActuatorImpl
import khome.entities.devices.Sensor
import khome.entities.devices.SensorImpl
import khome.observability.AsyncObserver
import khome.observability.AsyncObserverSuspendable
import khome.observability.Observer
import khome.observability.ObserverImpl
import khome.observability.WithHistory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get
import org.koin.core.inject
import kotlin.reflect.KClass

internal typealias SensorsByApiName = MutableMap<EntityId, SensorImpl<*>>
internal typealias ActuatorsByApiName = MutableMap<EntityId, ActuatorImpl<*>>
internal typealias ActuatorsByEntity = MutableMap<ActuatorImpl<*>, EntityId>

interface KhomeApplication {
    suspend fun run()
    fun <S> createSensor(id: EntityId, type: KClass<*>): Sensor<S>
    fun <S> createActuator(id: EntityId, type: KClass<*>): Actuator<S>
    fun <S> createObserver(f: (WithHistory<State<S>>) -> Unit): Observer<State<S>>
    fun <S> createAsyncObserver(f: AsyncObserverSuspendable<S>): Observer<State<S>>
}

@OptIn(
    ExperimentalStdlibApi::class,
    KtorExperimentalAPI::class,
    ObsoleteCoroutinesApi::class,
    ExperimentalCoroutinesApi::class
)
class KhomeApplicationImpl : KhomeApplication {
    private val koin = object : KhomeKoinComponent {}
    private val khomeClient: KhomeClient by koin.inject()

    private val sensorsByApiName: SensorsByApiName = mutableMapOf()
    private val actuatorsByApiName: ActuatorsByApiName = mutableMapOf()
    private val actuatorsByEntity: ActuatorsByEntity = mutableMapOf()

    override fun <S> createSensor(id: EntityId, type: KClass<*>): Sensor<S> =
        SensorImpl<S>(type).also { registerSensor(id, it) }

    override fun <S> createActuator(id: EntityId, type: KClass<*>): Actuator<S> =
        ActuatorImpl<S>(type).also { registerActuator(id, it) }

    override fun <S> createObserver(f: (WithHistory<State<S>>) -> Unit): Observer<State<S>> =
        ObserverImpl(f)

    override fun <S> createAsyncObserver(f: AsyncObserverSuspendable<S>): Observer<State<S>> =
        AsyncObserver(f)

    private fun registerSensor(entityId: EntityId, sensor: SensorImpl<*>) {
        sensorsByApiName[entityId] = sensor
    }

    private fun registerActuator(entityId: EntityId, actuator: ActuatorImpl<*>) {
        actuatorsByApiName[entityId] = actuator
        actuatorsByEntity[actuator] = entityId
    }

    internal fun <State> enqueueStateChange(actuator: ActuatorImpl<State>, value: State) {
        val entityId = actuatorsByEntity[actuator] ?: throw RuntimeException("Entity not registered: $actuator")
        println("Would soon send '${value.toString()}' as new state for ${entityId.domain}.${entityId.name}")
        // todo: Add to queue to be processed in connection thread
    }

    override suspend fun run() =
        khomeClient.startSession {

            Authenticator(this, get()).runBootSequence()

            ServiceStoreInitializer(this, get(), get()).runBootSequence()

            KhomeModulesInitializer(this).runBootSequence()

            EntityStateInitializer(
                this,
                get(),
                SensorStateUpdater(sensorsByApiName),
                ActuatorStateUpdater(actuatorsByApiName)
            ).runBootSequence()

            StateChangeEventSubscriber(this, get()).runBootSequence()

            EventResponseConsumer(
                this,
                get(),
                SensorStateUpdater(sensorsByApiName),
                ActuatorStateUpdater(actuatorsByApiName)
            ).runBootSequence()
        }
}
