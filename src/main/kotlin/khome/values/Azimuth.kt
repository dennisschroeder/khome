@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Azimuth private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<Azimuth> {
        override fun <P> from(value: P): Azimuth {
            return Azimuth((value as Double))
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Azimuth): P {
            return value.value as P
        }
    }
}

val Double.azimuth
    get() = Azimuth.from(this)

val Int.azimuth
    get() = Azimuth.from(this.toDouble())
