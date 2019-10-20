package khome.core.entities

import khome.Khome
import khome.core.State
import khome.core.StateStore
import khome.core.dependencyInjection.KhomePublicKoinComponent
import khome.core.dependencyInjection.internalRef
import khome.listening.getState
import khome.core.entities.exceptions.EntityNotFoundException
import khome.core.logger
import khome.listening.exceptions.EntityStateAttributeNotFoundException

abstract class AbstractEntity<StateValueType>(
    override val domain: String,
    override val name: String
) : KhomePublicKoinComponent(), EntityInterface {

    final override val id: String get() = "$domain.$name"
    override val state: State get() = getState(this)
    private val stateStore = internalRef<StateStore>()

    init {
        if (id !in stateStore) throw EntityNotFoundException("The Entity with id: $id was not found.")
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
