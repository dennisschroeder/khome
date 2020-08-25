package khome.extending.entities.sensors

import khome.KhomeApplication
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Sensor
import java.time.Instant

typealias LuminanceSensor = Sensor<LuminanceState, LuminanceAttributes>

@Suppress("FunctionName")
fun KhomeApplication.LuminanceSensor(objectId: String): LuminanceSensor = Sensor(objectId)

data class LuminanceState(override val value: Double) : State<Double>

data class LuminanceAttributes(
    val unitOfMeasurement: String,
    override val userId: String?,
    override val lastChanged: Instant,
    override val lastUpdated: Instant,
    override val friendlyName: String
) : Attributes

fun LuminanceSensor.isBrighterThan(value: Double) = measurement.value > value
fun LuminanceSensor.isDarkerThan(value: Double) = measurement.value < value
