@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Step private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<Step> {
        override fun <P> from(value: P): Step {
            return Step((value as Double))
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Step): P {
            return value.value as P
        }
    }
}
