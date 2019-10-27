package khome.core.entities

import io.ktor.util.KtorExperimentalAPI
import khome.core.State
import khome.core.StateStore
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.entities.exceptions.EntityNotFoundException
import khome.listening.exceptions.EntityStateAttributeNotFoundException
import khome.listening.exceptions.EntityStateNotFoundException
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.inject

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
abstract class AbstractEntity<StateValueType>(
    override val domain: String,
    override val name: String
) : KhomeKoinComponent(), EntityInterface {
    private val stateStore: StateStore by inject()
    final override val id: String get() = "$domain.$name"
    override val state: State get() = stateStore[id]
        ?: throw EntityStateNotFoundException("Could not fetch state object for entity: $id")

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

    override fun toString()= id

}
