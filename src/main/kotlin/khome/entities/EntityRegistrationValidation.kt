package khome.entities

import khome.ActuatorsByApiName
import khome.SensorsByApiName
import khome.values.EntityId
import mu.KotlinLogging

internal class EntityRegistrationValidation(
    private val actuatorsByApiName: ActuatorsByApiName,
    private val sensorsByApiName: SensorsByApiName
) {
    private val logger = KotlinLogging.logger { }

    private val failedIds = mutableListOf<EntityId>()

    fun validate(entityIds: List<EntityId>) {
        runActuatorsCheck(entityIds)
        runSensorsCheck(entityIds)

        if (failedIds.isEmpty()) {
            runSuccessProtocol()
            return
        }

        runFailureProtocol()
    }

    private fun runActuatorsCheck(entityIds: List<EntityId>) {
        actuatorsByApiName.forEach { entry ->
            if (entry.key !in entityIds) failedIds.add(entry.key)
        }
    }

    private fun runSensorsCheck(entityIds: List<EntityId>) {
        sensorsByApiName.forEach { entry ->
            if (entry.key !in entityIds) failedIds.add(entry.key)
        }
    }

    private fun runSuccessProtocol() {
        logger.info { "Entity registration validation succeeded" }
    }

    private fun runFailureProtocol() {
        logger.error {
            """
            
            
            ################ ERROR #################     
            
            Entity registration validation failed!
            Could not register the following entities:
            
            ${failedIds.joinToString("\n")}
            
            These entities could not be found in your 
            homeassistant instance. Please check your
            definitions.
            
            ################ ERROR #################
            
            """.trimIndent()
        }

        throw EntityRegistrationValidationException("Entity registration validation failed!")
    }
}
