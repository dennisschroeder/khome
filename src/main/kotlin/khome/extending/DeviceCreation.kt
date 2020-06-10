@file:Suppress("FunctionName")

package khome.extending

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType.TURN_OFF
import khome.communicating.ServiceType.TURN_ON
import khome.core.Attributes
import khome.core.State
import khome.entities.EntityId

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
inline fun <reified S : State<*>, reified SA : Attributes> KhomeApplication.Sensor(id: EntityId) =
    Sensor<S, SA>(id, S::class, SA::class)

inline fun <reified S : State<*>, reified SA : Attributes> KhomeApplication.Actuator(
    id: EntityId,
    serviceCommandResolver: ServiceCommandResolver<S>
) = Actuator<S, SA>(id, S::class, SA::class, serviceCommandResolver)
