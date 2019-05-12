package khome.core.entities

import khome.core.State

interface EntityInterface {
    val domain: String
    val service: String
    val entityId: String
    val state: Lazy<State>
    val attributes: Lazy<Map<String, Any>>
}
