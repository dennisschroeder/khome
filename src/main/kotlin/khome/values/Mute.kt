@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Mute private constructor(val value: Boolean) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<Mute> {
        override fun <P> from(value: P): Mute {
            return Mute((value as Boolean))
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Mute): P {
            return value.value as P
        }

        val TRUE: Mute get() = from(true)

        val FALSE: Mute get() = from(false)
    }
}
