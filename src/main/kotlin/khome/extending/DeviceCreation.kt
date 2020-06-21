package khome.extending

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType.TURN_OFF
import khome.communicating.ServiceType.TURN_ON
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State

fun mapSwitchable(switchableValue: SwitchableValue) =
    when (switchableValue) {
        SwitchableValue.ON -> DefaultResolvedServiceCommand(
            service = TURN_ON,
            serviceData = EntityIdOnlyServiceData()
        )
        SwitchableValue.OFF -> DefaultResolvedServiceCommand(
            service = TURN_OFF,
            serviceData = EntityIdOnlyServiceData()
        )
    }

/**
 * Base helper
 */

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Sensor(id: EntityId) =
    Sensor<S, A>(id, S::class, A::class)

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Actuator(
    id: EntityId,
    serviceCommandResolver: ServiceCommandResolver<S>
) = Actuator<S, A>(id, S::class, A::class, serviceCommandResolver)
