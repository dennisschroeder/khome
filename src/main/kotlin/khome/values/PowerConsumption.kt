@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class PowerConsumption private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<PowerConsumption> {
        override fun <P> from(value: P): PowerConsumption {
            return PowerConsumption((value as Double))
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: PowerConsumption): P {
            return value.value as P
        }
    }
}
