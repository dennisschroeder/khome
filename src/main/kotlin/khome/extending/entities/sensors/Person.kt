package khome.extending.entities.sensors

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.entities.Attributes
import khome.values.EntityId
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.PersonId
import khome.values.UserId
import khome.values.domain
import java.time.Instant

typealias Person<reified S> = Sensor<S, PersonAttributes>

@Suppress("FunctionName")
inline fun <reified S : State<*>> KhomeApplication.Person(objectId: ObjectId): Person<S> =
    Sensor(EntityId.fromPair("person".domain to objectId))

data class PersonState(override val value: PersonStateValue) : State<PersonStateValue>

enum class PersonStateValue {
    @SerializedName("home")
    HOME,

    @SerializedName("not_home")
    NOT_HOME
}

data class PersonAttributes(
    val source: EntityId,
    val id: PersonId,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

val Person<PersonState>.isHome
    get() = measurement.value == PersonStateValue.HOME

val Person<PersonState>.isAway
    get() = measurement.value != PersonStateValue.HOME

inline fun Person<PersonState>.onArrivedHome(
    crossinline f: Person<PersonState>.() -> Unit
) =
    attachObserver {
        if (measurementValueChangedFrom(PersonStateValue.HOME to PersonStateValue.NOT_HOME))
            f(this)
    }

inline fun Person<PersonState>.onLeftHome(
    crossinline f: Person<PersonState>.() -> Unit
) =
    attachObserver {
        if (measurementValueChangedFrom(PersonStateValue.HOME to PersonStateValue.NOT_HOME))
            f(this)
    }
