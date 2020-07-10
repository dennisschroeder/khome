package khome.extending.entities.sensors.binary

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.sensors.Sensor
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
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: String
) : Attributes
