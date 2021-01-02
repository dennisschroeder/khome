@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Initial private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<Initial> {
        override fun <P> from(value: P): Initial {
            return Initial((value as Double))
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Initial): P {
            return value.value as P
        }
    }
}
