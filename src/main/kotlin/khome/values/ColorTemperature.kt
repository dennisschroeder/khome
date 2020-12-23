@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class ColorTemperature private constructor(val value: Int, val unit: Unit) {
    override fun toString(): String = "$value.${unit.name.toLowerCase()}"

    companion object : KhomeTypeAdapter<ColorTemperature> {
        fun fromMired(value: Int) = ColorTemperature(value, Unit.MIRED)
        fun fromKelvin(value: Int) = ColorTemperature(value, Unit.KELVIN)

        override fun <P> from(value: P): ColorTemperature {
            return ColorTemperature(value as Int, Unit.MIRED)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: ColorTemperature): P {
            return value.value as P
        }
    }

    enum class Unit {
        MIRED, KELVIN
    }
}

val Int.mired
    get() = ColorTemperature.fromMired(this)

val Int.kelvin
    get() = ColorTemperature.fromKelvin(this)
