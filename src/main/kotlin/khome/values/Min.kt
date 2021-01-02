@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Min private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<Min> {
        override fun <P> from(value: P): Min {
            return Min((value as Double))
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Min): P {
            return value.value as P
        }
    }
}
