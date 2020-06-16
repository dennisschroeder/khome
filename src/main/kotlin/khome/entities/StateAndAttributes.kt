package khome.entities

import java.time.Instant

/**
 * The State interface
 *
 * Defines the minimum structure of a valid state object.
 *
 * @param T the type of the actual state value.
 */
interface State<T> {
    val value: T
}

/**
 * The Attributes interface
 *
 * Defines the minimum structure of a valid attributes object.
 * */
interface Attributes {
    val lastChanged: Instant
    val lastUpdated: Instant
    val friendlyName: String
}
