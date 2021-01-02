package khome.extending.entities.sensors

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import khome.values.FriendlyName
import khome.values.ObjectId
import khome.values.UnitOfMeasurement
import khome.values.UserId
import java.time.Instant

typealias LuminanceSensor = Sensor<LuminanceState, LuminanceAttributes>

@Suppress("FunctionName")
fun KhomeApplication.LuminanceSensor(objectId: ObjectId): LuminanceSensor = Sensor(objectId)

data class LuminanceState(override val value: Double) : State<Double>

data class LuminanceAttributes(
    val unitOfMeasurement: UnitOfMeasurement,
    override val userId: UserId?,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: FriendlyName
) : Attributes

fun LuminanceSensor.isBrighterThan(value: Double) = measurement.value > value
fun LuminanceSensor.isDarkerThan(value: Double) = measurement.value < value
