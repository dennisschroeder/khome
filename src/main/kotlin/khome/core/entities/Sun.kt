package khome.core.entities

import khome.core.State
import khome.listening.exceptions.EntityStateAttributeNotFoundException
import khome.listening.getState
import khome.listening.getStateAttributes

object Sun : EntityInterface {
    override val domain: String = "sun"
    override val service: String = "sun"
    override val entityId: String = "$domain.$service"
    override val state: Lazy<State> = lazy { getState(entityId) }
    override val attributes = lazy { getStateAttributes(entityId) }

    inline fun <reified AttributeValueType> getAttributeValue(name: String): AttributeValueType =
        Sun.state.value.getAttribute(name)
            ?: throw EntityStateAttributeNotFoundException("No state attribute for entity with id: $entityId and name: $name found.")
}