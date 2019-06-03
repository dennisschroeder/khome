package khome.core.entities


object Sun : AbstractEntity<String>("sun", "sun") {
    fun isUp() = state.getValue<String>() == "above_horizon"
    fun isDown() = state.getValue<String>() == "below_horizon"
}