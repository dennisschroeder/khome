package khome.core.entities

import khome.core.StateAttributes

interface EntitySubjectInterface {
    val entityId: EntityId
    val attributes: StateAttributes
}
