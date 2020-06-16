package khome.extending.actuators

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.Actuator
import khome.extending.DimmableLightAttributes
import khome.extending.DimmableLightServiceData
import khome.extending.DimmableLightState
import khome.extending.LightAttributes
import khome.extending.SwitchableState
import khome.extending.SwitchableValue
import khome.extending.mapSwitchable

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Light(
    objectId: String,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> =
    Actuator(EntityId("light", objectId), serviceCommandResolver)

@Suppress("FunctionName")
fun KhomeApplication.SwitchableLight(objectId: String): Actuator<SwitchableState, LightAttributes> =
    Light(objectId, ServiceCommandResolver { desiredState -> mapSwitchable(desiredState.value) })

@Suppress("FunctionName")
fun KhomeApplication.DimmableLight(objectId: String): Actuator<DimmableLightState, DimmableLightAttributes> =
    Light(objectId, ServiceCommandResolver { desiredState ->
        when (desiredState.value) {
            SwitchableValue.OFF -> {
                desiredState.brightness?.let { brightness ->
                    DefaultResolvedServiceCommand(
                        service = ServiceType.TURN_ON,
                        serviceData = DimmableLightServiceData(brightness)
                    )
                } ?: DefaultResolvedServiceCommand(
                    service = ServiceType.TURN_OFF,
                    serviceData = EntityIdOnlyServiceData()
                )
            }
            SwitchableValue.ON -> {
                desiredState.brightness?.let { brightness ->
                    DefaultResolvedServiceCommand(
                        service = ServiceType.TURN_ON,
                        serviceData = DimmableLightServiceData(brightness)
                    )
                } ?: DefaultResolvedServiceCommand(
                    service = ServiceType.TURN_ON,
                    serviceData = EntityIdOnlyServiceData()
                )
            }
        }
    })
