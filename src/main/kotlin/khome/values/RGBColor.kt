@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class RGBColor private constructor(val red: Int, val green: Int, val blue: Int) {
    companion object : KhomeTypeAdapter<RGBColor> {
        fun from(red: Int, green: Int, blue: Int) =
            from(listOf(red, green, blue))

        override fun <P> from(value: P): RGBColor {
            val integers = value as List<Int>
            check(integers.size == 3) { "To many values for RGB creation. List has size ${integers.size}. Allowed are exactly 3 values." }
            return RGBColor(red = integers[0], green = integers[1], blue = integers[2])
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: RGBColor): P {
            return intArrayOf(value.red, value.green, value.blue) as P
        }
    }
}
