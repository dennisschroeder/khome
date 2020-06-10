package khome.extending.actuators

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType
import khome.core.State
import khome.entities.EntityId
import khome.extending.Actuator
import khome.extending.DimmableLightAttributes
import khome.extending.LightAttributes
import khome.extending.DimmableLightServiceData
import khome.extending.SwitchableState
import khome.extending.SwitchableValue
import khome.extending.mapSwitchable

@Suppress("FunctionName")
fun KhomeApplication.SwitchableLight(objectId: String) =
    Actuator<SwitchableState, LightAttributes>(EntityId("light", objectId),
        ServiceCommandResolver { desiredState ->
            mapSwitchable(desiredState.value)
        })

data class DimmableLightState(override val value: SwitchableValue, val brightness: Int?) : State<SwitchableValue>

@Suppress("FunctionName")
fun KhomeApplication.DimmableLight(objectId: String) =
    Actuator<DimmableLightState, DimmableLightAttributes>(EntityId("light", objectId),
        ServiceCommandResolver { desiredState ->
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
                    } ?: DefaultResolvedServiceCommand(service = ServiceType.TURN_ON, serviceData = EntityIdOnlyServiceData())
                }
            }
        })
