package khome.communicating

import khome.core.State

interface DesiredState<T> : State<T> {
    override var value: T
}
