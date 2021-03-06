@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Brightness private constructor(val value: Int) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<Brightness> {
        override fun <P> from(value: P): Brightness {
            return Brightness(value as Int)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Brightness): P {
            return value.value as P
        }
    }
}

val Int.brightness
    get() = Brightness.from(this)
