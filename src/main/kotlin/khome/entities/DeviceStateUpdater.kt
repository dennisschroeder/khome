package khome.entities

import io.ktor.util.KtorExperimentalAPI
import khome.ActuatorsByApiName
import khome.SensorsByApiName
import khome.core.StateResponse
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging

internal class ActuatorStateUpdater(private val actuatorsByApiName: ActuatorsByApiName) {
    private val logger = KotlinLogging.logger { }

    @ObsoleteCoroutinesApi
    @KtorExperimentalAPI
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
        }
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
        }
    }
}
