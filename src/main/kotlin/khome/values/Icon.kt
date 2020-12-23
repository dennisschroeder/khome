@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Icon private constructor(val value: String) {
    override fun toString(): String = "$value.icon"

    companion object : KhomeTypeAdapter<Icon> {
        override fun <P> from(value: P): Icon {
            return Icon(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Icon): P {
            return value.value as P
        }
    }
}

val String.icon
    get() = Icon.from(this)
