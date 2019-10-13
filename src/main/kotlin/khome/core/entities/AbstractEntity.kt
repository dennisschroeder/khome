package khome.core.entities

import khome.Khome
import khome.core.State
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.listening.getState
import khome.core.entities.exceptions.EntityNotFoundException
import khome.listening.exceptions.EntityStateAttributeNotFoundException

open class AbstractEntity<StateValueType>(
    override val domain: String,
    override val name: String
) : KhomeKoinComponent(), EntityInterface {

    final override val id: String get() = "$domain.$name"
    override val state: State get() = getState(this)

    init {
        if (id !in Khome.states) throw EntityNotFoundException("The Entity with id: $id was not found.")
    }

    override val attributes: Map<String, Any> get() = state.attributes
    override val friendlyName: String get() = getAttributeValue("friendly_name")

    @Suppress("UNCHECKED_CAST")
    val stateValue: StateValueType
        get() = state.state as StateValueType

    inline fun <reified AttributeValueType> getAttributeValue(key: String): AttributeValueType =
        state.getAttribute(key)
            ?: throw EntityStateAttributeNotFoundException("No state attribute for entity with name: $id and name: $name found.")
}
