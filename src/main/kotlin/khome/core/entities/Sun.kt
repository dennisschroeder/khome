package khome.core.entities

import khome.core.State
import khome.listening.exceptions.EntityStateAttributeNotFoundException
import khome.listening.getState
import khome.listening.getStateAttributes

object Sun : EntityInterface {
    override val domain: String = "sun"
    override val name: String = "sun"
    override val entityId: String = "$domain.$name"
    override val state: State by lazy { getState(entityId) }
    override val attributes: Map<String, Any> by lazy { getStateAttributes(entityId) }

    inline fun <reified AttributeValueType> getAttributeValue(name: String): AttributeValueType =
        Sun.state.getAttribute(name)
            ?: throw EntityStateAttributeNotFoundException("No state attribute for entity with name: $entityId and name: $name found.")
}