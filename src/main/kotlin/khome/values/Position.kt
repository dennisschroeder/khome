@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Position private constructor(val value: Int) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<Position> {
        override fun <P> from(value: P): Position {
            return Position((value as Int))
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Position): P {
            return value.value as P
        }
    }
}

val Int.pctPosition
    get() = Position.from(this)
