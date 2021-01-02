package khome.extending.entities.sensors

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
import khome.observability.Switchable
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.PersonId
import khome.values.UserId
import khome.values.Zone
import khome.values.domain
import khome.values.zone
import java.time.Instant

typealias Person = Sensor<PersonState, PersonAttributes>

@Suppress("FunctionName")
fun KhomeApplication.Person(objectId: ObjectId): Person =
    Sensor(EntityId.fromPair("person".domain to objectId))

data class PersonState(override val value: Zone) : State<Zone>

data class PersonAttributes(
    val source: EntityId,
    val id: PersonId,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

val Person.isHome
    get() = measurement.value == "home".zone

val Person.isAway
    get() = measurement.value != "home".zone

inline fun Person.onArrivedHome(crossinline f: Person.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom("home".zone to "not_home".zone, f)

inline fun Person.onLeftHome(crossinline f: Person.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom("not_home".zone to "home".zone, f)
