package khome.core.entities

import khome.core.State
import khome.listening.getState
import khome.listening.getStateAttributes
import khome.listening.exceptions.EntityStateNotFoundException
import khome.listening.exceptions.EntityStateAttributeNotFoundException

abstract class AbstractEntity(entityDomain: String, entityName: String) : EntityInterface {
    override val domain: String = entityDomain
    override val name: String = entityName
    override val id: String get() = "$domain.$name"
    override val state: State by lazy { getState(id) }
    override val attributes: Map<String, Any> by lazy { getStateAttributes(id) }
    override val friendlyName: String = getAttributeValue("friendly_name")

    inline fun <reified StateValueType> getStateValue(): StateValueType =
        state.getValue<StateValueType>()
            ?: throw throw EntityStateNotFoundException("No state for entity with id: $id found.")

    inline fun <reified AttributeValueType> getAttributeValue(name: String): AttributeValueType =
        state.getAttribute(name)
            ?: throw EntityStateAttributeNotFoundException("No state attribute for entity with name: $id and name: $name found.")
}