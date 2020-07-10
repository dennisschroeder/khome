package khome.extending.entities.actuators

import com.google.gson.annotations.SerializedName
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
import java.time.Instant

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
                    serviceData = PositionableCoverServiceData(
                        position
                    )
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

data class CoverState(override val value: CoverValue, val currentPosition: Int? = null) : State<CoverValue>

enum class CoverValue {
    @SerializedName("open") OPEN,
    @SerializedName("closed") CLOSED
}

enum class Working {
    @SerializedName("Yes") YES,
    @SerializedName("No") NO
}

data class PositionableCoverAttributes(
    val working: Working,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: String
) : Attributes

data class PositionableCoverServiceData(val position: Int) : DesiredServiceData()
