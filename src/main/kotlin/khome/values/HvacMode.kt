@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class HvacMode private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<HvacMode> {
        override fun <P> from(value: P): HvacMode {
            return HvacMode(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: HvacMode): P {
            return value.value as P
        }
    }
}

val String.hvacMode
    get() = HvacMode.from(this)
