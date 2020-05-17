package khome.entities

import khome.ActuatorsByApiName
import khome.SensorsByApiName
import khome.core.StateResponse
import mu.KotlinLogging

internal class ActuatorStateUpdater(private val actuatorsByApiName: ActuatorsByApiName) {
    private val logger = KotlinLogging.logger { }

    @ExperimentalStdlibApi
    operator fun invoke(stateResponse: StateResponse) {
        actuatorsByApiName[stateResponse.entityId]?.let { entity ->
            entity.trySetActualStateFromAny(
                lastChanged = stateResponse.lastChanged,
                newValue = stateResponse.state,
                attributes = stateResponse.attributes,
                lastUpdated = stateResponse.lastUpdated
            )
            logger.debug { "Updated state for entity: ${stateResponse.entityId} with: $stateResponse" }
        } ?: logger.debug { "No entity registered with id: ${stateResponse.entityId}. Entity could not be updated." }
    }
}

internal class SensorStateUpdater(private val sensorsByApiName: SensorsByApiName) {
    private val logger = KotlinLogging.logger { }

    @ExperimentalStdlibApi
    operator fun invoke(stateResponse: StateResponse) {
        sensorsByApiName[stateResponse.entityId]?.let { entity ->
            entity.trySetMeasurementFromAny(
                lastChanged = stateResponse.lastChanged,
                newValue = stateResponse.state,
                attributes = stateResponse.attributes,
                lastUpdated = stateResponse.lastUpdated
            )
            logger.debug { "Updated state for entity: ${stateResponse.entityId} with: $stateResponse" }
        } ?: logger.debug { "No entity registered with id: ${stateResponse.entityId}. Entity could not be updated." }
    }
}
