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
    val options: List<String>,
    val editable: Boolean,
    val friendly_name: String
)

data class InputDateAttributes(
    val editable: Boolean,
    val has_date: Boolean,
    val has_time: Boolean,
    val year: Int,
    val month: Int,
    val day: Int,
    val friendly_name: String
)

data class InputTimeAttributes(
    val editable: Boolean,
    val has_date: Boolean,
    val has_time: Boolean,
    val timestamp: Int,
    val friendly_name: String
)

data class InputDateTimeAttributes(
    val editable: Boolean,
    val has_date: Boolean,
    val has_time: Boolean,
    val year: Int,
    val month: Int,
    val day: Int,
    val timestamp: Int,
    val friendly_name: String
)
