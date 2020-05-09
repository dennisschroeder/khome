package khome.core.entities

import mu.KotlinLogging
import org.koin.core.Koin

internal class EntityUpdater(private val entityIdToEntityTypeMap: EntityIdToEntityTypeMap, private val koin: Koin) {
    private val logger = KotlinLogging.logger { }

    operator fun invoke(entityId: EntityId, mutation: EntitySubject<*>.() -> Unit) {
        entityIdToEntityTypeMap[entityId]?.let { clazz ->
            koin.get<EntitySubject<*>>(clazz, null, null).apply(mutation)
        } ?: logger.debug { "No entity registered with id: $entityId. Entity could not be updated." }
    }
}
