package khome.extending

typealias AttributesMap = Map<String, Any>

data class InputTextAttributes(
    val editable: Boolean,
    val min: Int,
    val max: Int,
    val pattern: String,
    val mode: String,
    val friendly_name: String
)

data class InputNumberAttributes(
    val initial: Float,
    val editable: Boolean,
    val min: Float,
    val max: Float,
    val step: Float,
    val mode: String,
    val friendly_name: String
)

data class InputBooleanAttributes(
    val editable: Boolean,
    val friendly_name: String,
    val icon: String
)

data class InputSelectAttributes(
    val options: List<String>, //List of options as comma separated string
    val editable: Boolean,
    val friendly_name: String
)
