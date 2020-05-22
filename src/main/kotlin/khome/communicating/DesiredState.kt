package khome.communicating

interface DesiredState<T> {
    var value: T?
    val attributes: Map<String, Any>?
}

internal data class DesiredStateImpl<T>(
    override var value: T?,
    override val attributes: Map<String, Any>?
) : DesiredState<T>
