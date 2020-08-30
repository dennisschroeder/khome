package khome.extending.entities.sensors.binary

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.sensors.Sensor
import khome.extending.entities.sensors.measurementValueChangedFrom
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope
import java.time.Instant

typealias ContactSensor = Sensor<ContactState, ContactAttributes>

@Suppress("FunctionName")
fun KhomeApplication.ContactSensor(objectId: String): ContactSensor = Sensor(objectId)

data class ContactState(override val value: ContactStateValue) : State<ContactStateValue>

enum class ContactStateValue {
    @SerializedName("open")
    OPEN,

    @SerializedName("closed")
    CLOSED
}

data class ContactAttributes(
    override val userId: String?,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: String
) : Attributes

val ContactSensor.isOpen
    get() = measurement.value == ContactStateValue.OPEN

val ContactSensor.isClosed
    get() = measurement.value == ContactStateValue.CLOSED

inline fun ContactSensor.onOpened(crossinline f: ContactSensor.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (measurementValueChangedFrom(ContactStateValue.CLOSED to ContactStateValue.OPEN))
            f(this, observer)
    }

inline fun ContactSensor.onOpenedAsync(crossinline f: suspend ContactSensor.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (measurementValueChangedFrom(ContactStateValue.CLOSED to ContactStateValue.OPEN))
            f(this, observer, coroutineScope)
    }

inline fun ContactSensor.onClosed(crossinline f: ContactSensor.(Switchable) -> Unit) =
    attachObserver { observer ->
        if (measurementValueChangedFrom(ContactStateValue.OPEN to ContactStateValue.CLOSED))
            f(this, observer)
    }

inline fun ContactSensor.onClosedAsync(crossinline f: suspend ContactSensor.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, coroutineScope ->
        if (measurementValueChangedFrom(ContactStateValue.OPEN to ContactStateValue.CLOSED))
            f(this, observer, coroutineScope)
    }
