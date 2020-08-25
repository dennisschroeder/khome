package khome.extending.entities.actuators.light

import khome.KhomeApplication
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import java.time.Instant

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Light(
    objectId: String,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> = Actuator(EntityId("light", objectId), serviceCommandResolver)

data class LightAttributes(
    val supported_features: Int,
    override val userId: String?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes
