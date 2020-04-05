package khome.core.entities

import khome.core.StateStoreEntry

interface EntityInterface {
    val domain: String
    val name: String
    val id: String
    val states: StateStoreEntry
}
