package khome.core.entities

abstract class EntityCollection<EntityType : EntitySubjectInterface>(vararg entities: EntityType) : Iterable<EntityType> {
    private val list = listOf(*entities)
    override fun iterator() = list.iterator()
}
