@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Option private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<Option> {
        override fun <P> from(value: P): Option {
            return Option(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Option): P {
            return value.value as P
        }
    }
}

val String.option
    get() = Option.from(this)

val Enum<*>.option
    get() = Option.from(this.name)
