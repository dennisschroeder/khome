package khome.core.entities

import io.ktor.util.KtorExperimentalAPI
import khome.core.NewState
import khome.core.OldState
import khome.core.StateInterface
import khome.core.StateStoreEntry
import khome.core.StateStoreInterface
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.entities.exceptions.EntityNotFoundException
import khome.core.exceptions.InvalidAttributeValueTypeException
import khome.core.exceptions.InvalidStateValueTypeException
import khome.listening.exceptions.EntityStatesNotFoundException
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import org.koin.core.inject

@Suppress("MemberVisibilityCanBePrivate")
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
abstract class AbstractEntity<StateValueType>(
    override val domain: String,
    override val name: String
) : KhomeKoinComponent(), EntityInterface {
    private val stateStore: StateStoreInterface by inject()
    final override val id: String get() = "$domain.$name"
    final override val states: StateStoreEntry
        get() = stateStore[id]
            ?: throw EntityStatesNotFoundException("Could not fetch state object for entity: $id")

    val oldState: OldState
        get() = states.oldState

    val newState: NewState
        get() = states.newState

    init {
        if (id !in stateStore) throw EntityNotFoundException("Could not get data for entity: $id")

        @Suppress("UNCHECKED_CAST")
        if (oldState.state as? StateValueType == null) throw InvalidStateValueTypeException("Could not cast old state vale to type parameter of entity: $id ")

        @Suppress("UNCHECKED_CAST")
        if (newState.state as? StateValueType == null) throw InvalidStateValueTypeException("Could not cast new state vale to type parameter of entity: $id ")
    }

    override fun toString() = id
}

inline fun <reified T> StateInterface.getAttribute(key: String): T {
    return attributes[key] as? T ?: throw InvalidAttributeValueTypeException(
        "Attribute value for $key is of type: ${(attributes[key]
            ?: error("Key not valid"))::class}."
    )
}

suspend fun StateInterface.hasStateChangedAfter(millis: Long): Boolean {
    val initial = state
    delay(millis)
    val afterDelay = state
    return initial == afterDelay
}

suspend fun StateInterface.hasAttributesChangedAfter(millis: Long, vararg attributes: String): Boolean {
    val results = attributes.map { attribute ->
        hasAttributeChangedAfter(millis, attribute)
    }

    return results.contains(false)
}

suspend fun StateInterface.hasAttributeChangedAfter(millis: Long, attribute: String): Boolean {
    val initial = attributes[attribute]
    delay(millis)
    val afterDelay = attributes[attribute]
    return initial == afterDelay
}

suspend fun StateInterface.onlyIfStateHasNotChangedAfter(millis: Long, block: suspend () -> Unit) {
    if (hasStateChangedAfter(millis)) block()
}

suspend fun StateInterface.onlyIfAttributeHasNotChangedAfter(millis: Long, attribute: String, block: suspend () -> Unit) {
    if (hasAttributeChangedAfter(millis, attribute)) block()
}
