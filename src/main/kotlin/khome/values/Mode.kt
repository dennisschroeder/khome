@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Mode private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<Mode> {
        override fun <P> from(value: P): Mode {
            return Mode(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Mode): P {
            return value.value as P
        }
    }
}

val String.mode
    get() = Mode.from(this)
