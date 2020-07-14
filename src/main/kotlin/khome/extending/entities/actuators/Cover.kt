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
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope
import java.time.Instant

typealias PositionableCover = Actuator<PositionableCoverState, PositionableCoverAttributes>

@Suppress("FunctionName")
inline fun <reified S : State<*>, reified A : Attributes> KhomeApplication.Cover(
    objectId: String,
    serviceCommandResolver: ServiceCommandResolver<S>
): Actuator<S, A> = Actuator(EntityId("cover", objectId), serviceCommandResolver)

@Suppress("FunctionName")
fun KhomeApplication.PositionableCover(objectId: String): PositionableCover =
    Cover(objectId, ServiceCommandResolver { state ->
        when (state.value) {
            PositionableCoverValue.OPEN -> state.currentPosition?.let { position ->
                DefaultResolvedServiceCommand(
                    service = ServiceType.SET_COVER_POSITION,
                    serviceData = PositionableCoverServiceData(position)
                )
            } ?: DefaultResolvedServiceCommand(
                service = ServiceType.OPEN_COVER,
                serviceData = EntityIdOnlyServiceData()
            )

            PositionableCoverValue.CLOSED -> DefaultResolvedServiceCommand(
                service = ServiceType.CLOSE_COVER,
                serviceData = EntityIdOnlyServiceData()
            )
        }
    })

data class PositionableCoverState(override val value: PositionableCoverValue, val currentPosition: Int? = null) : State<PositionableCoverValue>

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
    override val userId: String?,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: String
) : Attributes

data class PositionableCoverServiceData(val position: Int) : DesiredServiceData()

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

fun PositionableCover.setCoverPosition(position: Int) {
    desiredState = PositionableCoverState(PositionableCoverValue.OPEN, position)
}

fun PositionableCover.onStartWorking(f: PositionableCover.() -> Unit) =
    attachObserver {
        if (history[1].attributes.working == Working.NO &&
            attributes.working == Working.YES
        ) {
            f(this)
        }
    }

fun PositionableCover.onStartWorkingAsync(f: suspend PositionableCover.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, scope ->
        if (history[1].attributes.working == Working.NO &&
            attributes.working == Working.YES
        ) {
            f(this, observer, scope)
        }
    }

fun PositionableCover.onStopWorking(f: PositionableCover.() -> Unit) =
    attachObserver {
        if (history[1].attributes.working == Working.YES &&
            attributes.working == Working.NO
        ) {
            f(this)
        }
    }

fun PositionableCover.onStopWorkingAsync(f: suspend PositionableCover.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, scope ->
        if (history[1].attributes.working == Working.YES &&
            attributes.working == Working.NO
        ) {
            f(this, observer, scope)
        }
    }

fun PositionableCover.onClosing(f: PositionableCover.() -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(PositionableCoverValue.OPEN to PositionableCoverValue.CLOSED))
            f(this)
    }

fun PositionableCover.onClosingAsync(f: suspend PositionableCover.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, scope ->
        if (stateValueChangedFrom(PositionableCoverValue.OPEN to PositionableCoverValue.CLOSED)) f(this, observer, scope)
    }

fun PositionableCover.onOpening(f: PositionableCover.() -> Unit) =
    attachObserver {
        if (stateValueChangedFrom(PositionableCoverValue.CLOSED to PositionableCoverValue.OPEN))
            f(this)
    }

fun PositionableCover.onOpeningAsync(f: suspend PositionableCover.(Switchable, CoroutineScope) -> Unit) =
    attachAsyncObserver { observer, scope ->
        if (stateValueChangedFrom(PositionableCoverValue.CLOSED to PositionableCoverValue.OPEN)) f(this, observer, scope)
    }
