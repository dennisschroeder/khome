package khome.extending.entities.actuators

import com.google.gson.annotations.SerializedName
import khome.KhomeApplication
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.DesiredServiceData
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.extending.entities.Actuator
import khome.observability.Switchable
import khome.values.EntityId
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.Position
import khome.values.UserId
import khome.values.domain
import khome.values.service
import java.time.Instant

typealias PositionableCover = Actuator<PositionableCoverState, PositionableCoverAttributes>

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Cover(
    objectId: ObjectId,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> = Actuator(EntityId.fromPair("cover".domain to objectId), serviceCommandResolver)

@Suppress("FunctionName")
fun KhomeApplication.PositionableCover(objectId: ObjectId): PositionableCover =
    Cover(
        objectId,
        ServiceCommandResolver { state ->
            when (state.value) {
                PositionableCoverValue.OPEN -> state.currentPosition?.let { position ->
                    DefaultResolvedServiceCommand(
                        service = "set_cover_position".service,
                        serviceData = PositionableCoverServiceData(position)
                    )
                } ?: DefaultResolvedServiceCommand(
                    service = "open_cover".service,
                    serviceData = EntityIdOnlyServiceData()
                )

                PositionableCoverValue.CLOSED -> DefaultResolvedServiceCommand(
                    service = "close_cover".service,
                    serviceData = EntityIdOnlyServiceData()
                )
            }
        }
    )

data class PositionableCoverState(
    override val value: PositionableCoverValue,
    val currentPosition: Position? = null
) : State<PositionableCoverValue>

enum class PositionableCoverValue {
    @SerializedName("open")
    OPEN,

    @SerializedName("closed")
    CLOSED
}

enum class Working {
    @SerializedName("Yes")
    YES,

    @SerializedName("No")
    NO
}

data class PositionableCoverAttributes(
    val working: Working,
    override val userId: UserId?,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: FriendlyName
) : Attributes

data class PositionableCoverServiceData(val position: Position) : DesiredServiceData()

val PositionableCover.isOpen
    get() = actualState.value == PositionableCoverValue.OPEN

val PositionableCover.isClosed
    get() = actualState.value == PositionableCoverValue.CLOSED

val PositionableCover.isWorking
    get() = attributes.working == Working.YES

fun PositionableCover.open() {
    desiredState = PositionableCoverState(PositionableCoverValue.OPEN)
}

fun PositionableCover.close() {
    desiredState = PositionableCoverState(PositionableCoverValue.CLOSED)
}

fun PositionableCover.setCoverPosition(position: Position) {
    desiredState = PositionableCoverState(PositionableCoverValue.OPEN, position)
}

fun PositionableCover.onStartedWorking(f: PositionableCover.() -> Unit) =
    attachObserver {
        if (history[1].attributes.working == Working.NO &&
            attributes.working == Working.YES
        ) {
            f(this)
        }
    }

fun PositionableCover.onStoppedWorking(f: PositionableCover.() -> Unit) =
    attachObserver {
        if (history[1].attributes.working == Working.YES &&
            attributes.working == Working.NO
        ) {
            f(this)
        }
    }

fun PositionableCover.onClosed(f: PositionableCover.(Switchable) -> Unit) =
    onStateValueChangedFrom(PositionableCoverValue.OPEN to PositionableCoverValue.CLOSED, f)

fun PositionableCover.onOpened(f: PositionableCover.(Switchable) -> Unit) =
    onStateValueChangedFrom(PositionableCoverValue.CLOSED to PositionableCoverValue.OPEN, f)
