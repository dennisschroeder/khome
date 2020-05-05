package khome.core.entities

import khome.core.State

interface EntitySubjectInterface {
    val domain: String
    val name: String
    val id: String
    val state: State
}
