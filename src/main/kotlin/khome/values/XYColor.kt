@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class XYColor private constructor(val x: Double, val y: Double) {
    @Suppress("UNCHECKED_CAST")
    companion object : KhomeTypeAdapter<XYColor> {
        fun from(x: Double, y: Double) =
            from(listOf(x, y))

        override fun <P> from(value: P): XYColor {
            val values = value as List<Double>
            check(values.size == 2) { "To many values for XYColor creation. ${values.size} values are too much. Allowed are exactly 2 values." }
            return XYColor(x = values[0], y = values[1])
        }

        override fun <P> to(value: XYColor): P {
            return doubleArrayOf(value.x, value.y) as P
        }
    }
}
