@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Elevation private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<Elevation> {
        override fun <P> from(value: P): Elevation {
            return Elevation((value as Double))
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Elevation): P {
            return value.value as P
        }
    }
}
