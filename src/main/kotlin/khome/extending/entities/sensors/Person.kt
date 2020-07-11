package khome.extending.entities.sensors

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Sensor
import khome.extending.entities.Sensor
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
