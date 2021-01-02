package khome.extending.entities.sensors.binary

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.sensors.Sensor
import khome.extending.entities.sensors.onMeasurementValueChangedFrom
import khome.observability.Switchable
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.UserId
import java.time.Instant

typealias ContactSensor = Sensor<ContactState, ContactAttributes>

@Suppress("FunctionName")
fun KhomeApplication.ContactSensor(objectId: ObjectId): ContactSensor = Sensor(objectId)

data class ContactState(override val value: ContactStateValue) : State<ContactStateValue>

enum class ContactStateValue {
    @SerializedName("open")
    OPEN,

    @SerializedName("closed")
    CLOSED
}

data class ContactAttributes(
    override val userId: UserId?,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: FriendlyName
) : Attributes

val ContactSensor.isOpen
    get() = measurement.value == ContactStateValue.OPEN

val ContactSensor.isClosed
    get() = measurement.value == ContactStateValue.CLOSED

inline fun ContactSensor.onOpened(crossinline f: ContactSensor.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom(ContactStateValue.CLOSED to ContactStateValue.OPEN, f)

inline fun ContactSensor.onClosed(crossinline f: ContactSensor.(Switchable) -> Unit) =
    onMeasurementValueChangedFrom(ContactStateValue.OPEN to ContactStateValue.CLOSED, f)
