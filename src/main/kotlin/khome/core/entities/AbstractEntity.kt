package khome.core.entities

import khome.core.State
import khome.listening.getState
import khome.listening.getStateAttributes
import khome.listening.exceptions.EntityStateNotFoundException
import khome.listening.exceptions.EntityStateAttributeNotFoundException

abstract class AbstractEntity(entityDomain: String, entityName: String) : EntityInterface {
    override val domain: String = entityDomain
    override val name: String = entityName
    override val entityId: String get() = "$domain.$name"
    override val state: State by lazy { getState(entityId) }
    override val attributes: Map<String, Any> by lazy { getStateAttributes(entityId) }

    inline fun <reified StateValueType> getStateValue(): StateValueType =
        Sun.state.getValue<StateValueType>()
            ?: throw throw EntityStateNotFoundException("No state for entity with id: $entityId found.")

    inline fun <reified AttributeValueType> getAttributeValue(name: String): AttributeValueType =
        Sun.state.getAttribute(name)
            ?: throw EntityStateAttributeNotFoundException("No state attribute for entity with name: ${Sun.entityId} and name: $name found.")
}