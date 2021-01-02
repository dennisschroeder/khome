package khome.entities

import com.google.gson.JsonObject
import io.ktor.util.KtorExperimentalAPI
import khome.ActuatorsByApiName
import khome.SensorsByApiName
import khome.values.EntityId
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging

internal class ActuatorStateUpdater(private val actuatorsByApiName: ActuatorsByApiName) {
    private val logger = KotlinLogging.logger { }

    @ObsoleteCoroutinesApi
    @KtorExperimentalAPI
    @ExperimentalStdlibApi
    operator fun invoke(newActualState: JsonObject, entityId: EntityId) {
        actuatorsByApiName[entityId]?.let { entity ->
            entity.trySetAttributesFromAny(newAttributes = newActualState)
            entity.trySetActualStateFromAny(newState = newActualState)
            logger.debug { "Updated state for entity: $entityId with: $newActualState" }
        }
    }
}

internal class SensorStateUpdater(private val sensorsByApiName: SensorsByApiName) {
    private val logger = KotlinLogging.logger { }

    @ExperimentalStdlibApi
    operator fun invoke(newActualState: JsonObject, entityId: EntityId) {
        sensorsByApiName[entityId]?.let { entity ->
            entity.trySetAttributesFromAny(newAttributes = newActualState)
            entity.trySetActualStateFromAny(newState = newActualState)
            logger.debug { "Updated state for entity: $entityId with: $newActualState" }
        }
    }
}
