@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class ColorName private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<ColorName> {
        override fun <P> from(value: P): ColorName {
            return ColorName(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: ColorName): P {
            return value.value as P
        }
    }
}

val String.color
    get() = ColorName.from(this)

val Enum<*>.color
    get() = ColorName.from(this.name)
