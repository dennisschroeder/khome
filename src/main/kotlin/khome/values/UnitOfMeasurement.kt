@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class UnitOfMeasurement private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<UnitOfMeasurement> {
        override fun <P> from(value: P): UnitOfMeasurement {
            return UnitOfMeasurement(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: UnitOfMeasurement): P {
            return value.value as P
        }
    }
}
