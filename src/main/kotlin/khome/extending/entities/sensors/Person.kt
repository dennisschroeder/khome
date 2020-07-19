package khome.extending.entities.sensors

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope
import java.time.Instant

typealias Person<reified S> = Sensor<S, PersonAttributes>

@Suppress("FunctionName")
inline fun <reified S : State<*>> KhomeApplication.Person(objectId: String): Person<S> = Sensor(EntityId("person", objectId))

data class PersonState(override val value: PersonStateValue) : State<PersonStateValue>

enum class PersonStateValue {
    @SerializedName("home")
    HOME,
    @SerializedName("not_home")
    NOT_HOME
}

data class PersonAttributes(
    val source: EntityId,
    val id: String,
    override val userId: String,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

val Person<PersonState>.isHome
    get() = measurement.value == PersonStateValue.HOME

val Person<PersonState>.isAway
    get() = measurement.value == PersonStateValue.NOT_HOME

inline fun Person<PersonState>.onArrival(crossinline f: Person<PersonState>.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (measurementValueChangedFrom(PersonStateValue.HOME to PersonStateValue.NOT_HOME))
            f(this, observer)
    }

inline fun Person<PersonState>.onArrivalAsync(crossinline f: suspend Person<PersonState>.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (measurementValueChangedFrom(PersonStateValue.HOME to PersonStateValue.NOT_HOME))
            f(this, observer, coroutineScope)
    }

inline fun Person<PersonState>.onLeaving(crossinline f: Person<PersonState>.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (measurementValueChangedFrom(PersonStateValue.HOME to PersonStateValue.NOT_HOME))
            f(this, observer)
    }

inline fun Person<PersonState>.onLeavingAsync(crossinline f: suspend Person<PersonState>.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (measurementValueChangedFrom(PersonStateValue.HOME to PersonStateValue.NOT_HOME))
            f(this, observer, coroutineScope)
    }
