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
import khome.extending.CoverState
import khome.extending.CoverValue
import khome.extending.PositionableCoverAttributes
import khome.extending.PositionableCoverServiceData

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Cover(
    objectId: String,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> = Actuator(EntityId("cover", objectId), serviceCommandResolver)

@Suppress("FunctionName")
fun KhomeApplication.PositionableCover(objectId: String): Actuator<CoverState, PositionableCoverAttributes> =
    Actuator(EntityId("cover", objectId), ServiceCommandResolver { state ->
        when (state.value) {
            CoverValue.OPEN -> state.currentPosition?.let { position ->
                DefaultResolvedServiceCommand(
                    service = ServiceType.SET_COVER_POSITION,
                    serviceData = PositionableCoverServiceData(position)
                )
            } ?: DefaultResolvedServiceCommand(
                service = ServiceType.OPEN_COVER,
                serviceData = EntityIdOnlyServiceData()
            )

            CoverValue.CLOSED -> DefaultResolvedServiceCommand(
                service = ServiceType.CLOSE_COVER,
                serviceData = EntityIdOnlyServiceData()
            )
        }
    })
