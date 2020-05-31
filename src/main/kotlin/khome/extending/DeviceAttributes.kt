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
