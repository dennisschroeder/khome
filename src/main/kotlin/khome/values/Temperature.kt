package khome.values

import khome.core.mapping.KhomeTypeAdapter

@Suppress("DataClassPrivateConstructor")
data class Temperature private constructor(val value: Double) {
    internal companion object : KhomeTypeAdapter<Temperature> {
        override fun <P> from(value: P): Temperature {
            return Temperature(value as Double)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Temperature): P {
            return value.value as P
        }
    }
}

val Double.degree
    get() = Temperature.from(this)

val Int.degree
    get() = Temperature.from(this.toDouble())
