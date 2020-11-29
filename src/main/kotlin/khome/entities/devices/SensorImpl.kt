package khome.entities.devices

import com.google.gson.JsonObject
import io.ktor.util.KtorExperimentalAPI
import khome.KhomeApplicationImpl
import khome.core.mapping.ObjectMapperInterface
import khome.core.observing.CircularBuffer
import khome.entities.Attributes
import khome.entities.State
import khome.errorHandling.ObserverExceptionHandler
import khome.observability.ObservableDelegateNoInitial
import khome.observability.Observer
import khome.observability.ObserverFunction
import khome.observability.ObserverImpl
import khome.observability.StateAndAttributes
import khome.observability.Switchable
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.reflect.KClass

internal class SensorImpl<S : State<*>, A : Attributes>(
    private val app: KhomeApplicationImpl,
    private val mapper: ObjectMapperInterface,
    private val stateType: KClass<*>,
    private val attributesType: KClass<*>
) : Sensor<S, A> {
    private val observers: MutableList<Observer<Sensor<S, A>>> = mutableListOf()
    override lateinit var attributes: A
    private val _history = CircularBuffer<StateAndAttributes<S, A>>(10)
    override var measurement: S by ObservableDelegateNoInitial(this, observers, _history)

    override val history: List<StateAndAttributes<S, A>>
        get() = _history.snapshot

    override val observerCount: Int
        get() = observers.size

    override fun attachObserver(observer: ObserverFunction<Sensor<S, A>>): Switchable =
        ObserverImpl(
            observer,
            ObserverExceptionHandler(app.observerExceptionHandlerFunction)
        ).also { observers.add(it) }

    @ObsoleteCoroutinesApi
    @KtorExperimentalAPI
    @ExperimentalStdlibApi
    fun trySetActualStateFromAny(newState: JsonObject) {
        @Suppress("UNCHECKED_CAST")
        measurement = mapper.fromJson(newState, stateType.java) as S
        checkNotNull(measurement.value) { "State value shall not be null. Please check your State definition " }
    }

    @ObsoleteCoroutinesApi
    @KtorExperimentalAPI
    fun trySetAttributesFromAny(newAttributes: JsonObject) {
        @Suppress("UNCHECKED_CAST")
        attributes = mapper.fromJson(newAttributes, attributesType.java) as A
    }
}
