package khome.extending.entities.actuators

import khome.KhomeApplication
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.values.EntityId
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.extending.entities.SwitchableState
import khome.extending.entities.mapSwitchable
import khome.values.ObjectId
import khome.values.UserId
import khome.values.domain
import java.time.Instant

typealias Switch<reified A> = Actuator<SwitchableState, A>
typealias PowerSwitch = Switch<PowerSwitchAttributes>

@Suppress("FunctionName")
inline fun <reified A : Attributes> KhomeApplication.Switch(objectId: ObjectId): Switch<A> =
    Actuator(EntityId.fromPair("switch".domain to objectId), ServiceCommandResolver { switchableState ->
        mapSwitchable(switchableState.value)
    })

@Suppress("FunctionName")
fun KhomeApplication.PowerSwitch(objectId: ObjectId): PowerSwitch = Switch(objectId)

data class PowerSwitchAttributes(
    val powerConsumption: Double,
    override val userId: UserId?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes
