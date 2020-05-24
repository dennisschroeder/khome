package khome.communicating

interface DesiredState<T> {
    var value: T?
    val attributes: CommandDataWithEntityId?
}

internal data class DesiredStateImpl<T>(
    override var value: T?,
    override val attributes: CommandDataWithEntityId?
) : DesiredState<T>
