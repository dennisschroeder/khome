package khome.entities.devices

import com.google.gson.JsonObject
import io.ktor.util.KtorExperimentalAPI
import khome.core.Attributes
import khome.core.State
import khome.core.mapping.ObjectMapper
import khome.observability.Observable
import khome.observability.ObservableHistoryNoInitialDelegate
import khome.observability.StateAndAttributes
import khome.observability.Switchable
import khome.observability.SwitchableObserver
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlin.reflect.KClass

interface Sensor<S : State<*>, SA : Attributes> : Observable {
    val measurement: S
    val attributes: SA
}

internal class SensorImpl<S : State<*>, SA : Attributes>(
    private val mapper: ObjectMapper,
    private val stateType: KClass<*>,
    private val attributesType: KClass<*>
) : Sensor<S, SA> {
    private val observers: MutableList<SwitchableObserver<S, SA, StateAndAttributes<S, SA>>> = mutableListOf()
    override lateinit var attributes: SA
    override var measurement: S by ObservableHistoryNoInitialDelegate(observers) { attributes }

    @Suppress("UNCHECKED_CAST")
    override fun attachObserver(observer: Switchable) {
        observers.add(observer as SwitchableObserver<S, SA, StateAndAttributes<S, SA>>)
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
        attributes = mapper.fromJson(newAttributes, attributesType.java) as SA
    }
}
