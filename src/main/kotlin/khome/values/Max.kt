@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Max private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<Max> {
        override fun <P> from(value: P): Max {
            return Max((value as Double))
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Max): P {
            return value.value as P
        }
    }
}
