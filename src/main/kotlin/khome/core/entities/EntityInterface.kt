package khome.core.entities

import khome.core.State
import khome.listening.registerStateChangeEvent

interface EntityInterface {
    val domain: String
    val name: String
    val id: String
    val state: State
    val attributes: Map<String, Any>
    val friendlyName: String
}
