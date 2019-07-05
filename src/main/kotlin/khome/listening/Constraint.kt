package khome.listening

import khome.core.State

data class Constraint(
    val newState: State,
    val oldState: State
)