package khome.core.entities

import khome.core.StateResponse
import mu.KotlinLogging
import org.koin.core.Koin

internal class EntityStateUpdater(
    private val entityIdToEntityTypeMap: EntityIdToEntityTypeMap,
    private val koin: Koin
) {
    private val logger = KotlinLogging.logger { }

    operator fun invoke(entityId: EntityId, stateResponse: StateResponse) {
        entityIdToEntityTypeMap[entityId]?.let { clazz ->
            koin.get<EntitySubject<*>>(clazz, null, null).setStateFromResponse(stateResponse)
            logger.debug { "Updated state for entity: ${stateResponse.entityId} with: $stateResponse" }
        } ?: logger.debug { "No entity registered with id: $entityId. Entity could not be updated." }
    }
}
