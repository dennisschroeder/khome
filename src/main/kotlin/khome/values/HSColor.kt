@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class HSColor private constructor(val hue: Double, val saturation: Double) {
    @Suppress("UNCHECKED_CAST")
    companion object : KhomeTypeAdapter<HSColor> {
        fun from(hue: Double, saturation: Double) =
            from(listOf(hue, saturation))

        override fun <P> from(value: P): HSColor {
            val values = value as List<Double>
            check(values.size == 2) { "To many values for HSColor creation. ${values.size} values are too much. Allowed are exactly 2 values." }
            return HSColor(hue = values[0], saturation = values[1])
        }

        override fun <P> to(value: HSColor): P {
            return doubleArrayOf(value.hue, value.saturation) as P
        }
    }
}
