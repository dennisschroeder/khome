package khome.entities.devices

import com.google.gson.JsonObject
import io.ktor.util.KtorExperimentalAPI
import khome.core.mapping.ObjectMapper
import khome.core.observing.CircularBuffer
import khome.entities.Attributes
import khome.entities.State
import khome.observability.ObservableHistoryNoInitialDelegate
import khome.observability.Observer
import khome.observability.StateAndAttributes
import khome.observability.SwitchableObserver
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.reflect.KClass

internal class SensorImpl<S : State<*>, A : Attributes>(
    private val mapper: ObjectMapper,
    private val stateType: KClass<*>,
    private val attributesType: KClass<*>
) : Sensor<S, A> {
    private val observers: MutableList<Observer<S, A, StateAndAttributes<S, A>>> = mutableListOf()
    override lateinit var attributes: A
    private val _history = CircularBuffer<StateAndAttributes<S, A>>(10)
    override var measurement: S by ObservableHistoryNoInitialDelegate(observers, _history) { attributes }

    @Suppress("UNCHECKED_CAST")
    override fun attachObserver(observer: SwitchableObserver<S, A>) {
        observers.add(observer as Observer<S, A, StateAndAttributes<S, A>>)
    }

    @ObsoleteCoroutinesApi
    @KtorExperimentalAPI
    @ExperimentalStdlibApi
    fun trySetActualStateFromAny(newState: JsonObject) {
        @Suppress("UNCHECKED_CAST")
        measurement = mapper.fromJson(newState, stateType.java) as S
    }

    @ObsoleteCoroutinesApi
    @KtorExperimentalAPI
    fun trySetAttributesFromAny(newAttributes: JsonObject) {
        @Suppress("UNCHECKED_CAST")
        attributes = mapper.fromJson(newAttributes, attributesType.java) as A
    }

    override val history: List<StateAndAttributes<S, A>>
        get() = _history.snapshot()
}
