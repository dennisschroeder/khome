package khome.extending.entities.actuators.light

import khome.KhomeApplication
import khome.communicating.DesiredServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.values.ColorName
import khome.values.ColorTemperature
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.UserId
import khome.values.domain
import java.time.Instant

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Light(
    objectId: ObjectId,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> = Actuator(EntityId.fromPair("light".domain to objectId), serviceCommandResolver)

data class LightAttributes(
    val supported_features: Int,
    override val userId: UserId?,
    override val friendlyName: FriendlyName,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class NamedColorServiceData(val color_name: ColorName) : DesiredServiceData()
data class KelvinServiceData(val kelvin: ColorTemperature) : DesiredServiceData()
