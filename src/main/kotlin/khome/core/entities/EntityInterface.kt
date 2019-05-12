package khome.core.entities

import khome.core.State

interface EntityInterface {
    val domain: String
    val name: String
    val entityId: String
    val state: State
    val attributes: Map<String, Any>
}
