package khome.extending.entities.actuators

import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType
import khome.entities.Attributes
import khome.entities.EntityId
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.extending.entities.SwitchableState
import khome.extending.entities.SwitchableValue
import khome.extending.entities.mapSwitchable
import java.time.Instant

typealias SwitchableLight = Actuator<SwitchableState, LightAttributes>
typealias DimmableLight = Actuator<DimmableLightState, DimmableLightAttributes>

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Light(
    objectId: String,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> =
    Actuator(EntityId("light", objectId), serviceCommandResolver)

@Suppress("FunctionName")
fun KhomeApplication.SwitchableLight(objectId: String): SwitchableLight =
    Light(objectId, ServiceCommandResolver { desiredState ->
        mapSwitchable(
            desiredState.value
        )
    })

@Suppress("FunctionName")
fun KhomeApplication.DimmableLight(objectId: String): DimmableLight =
    Light(objectId, ServiceCommandResolver { desiredState ->
        when (desiredState.value) {
            SwitchableValue.OFF -> {
                desiredState.brightness?.let { brightness ->
                    DefaultResolvedServiceCommand(
                        service = ServiceType.TURN_ON,
                        serviceData = DimmableLightServiceData(
                            brightness
                        )
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
                        serviceData = DimmableLightServiceData(
                            brightness
                        )
                    )
                } ?: DefaultResolvedServiceCommand(
                    service = ServiceType.TURN_ON,
                    serviceData = EntityIdOnlyServiceData()
                )
            }
        }
    })

data class DimmableLightState(override val value: SwitchableValue, val brightness: Int? = null) : State<SwitchableValue>

data class LightAttributes(
    val supported_features: Int,
    override val userId: String?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class DimmableLightAttributes(
    val powerConsumption: Double,
    val supported_features: Int,
    override val userId: String?,
    override val friendlyName: String,
    override val lastChanged: Instant,
    override val lastUpdated: Instant
) : Attributes

data class DimmableLightServiceData(private val brightness: Int) : DesiredServiceData()
