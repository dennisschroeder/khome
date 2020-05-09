package khome.core.entities

interface EntitySubjectInterface {
    val domain: String
    val id: String
    val entityId: EntityId
    val state: Any
}
