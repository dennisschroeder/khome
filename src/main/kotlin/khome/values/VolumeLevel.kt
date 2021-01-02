@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class VolumeLevel private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<VolumeLevel> {
        override fun <P> from(value: P): VolumeLevel {
            val volume = value as Double
            check(volume >= 0) { "Volume can not be a negative value" }
            check(volume <= 100) { "Volume can not be greater than 100(percent)" }
            return VolumeLevel(volume)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: VolumeLevel): P {
            return (value.value / 100) as P
        }
    }
}

val Double.pctVolume
    get() = VolumeLevel.from(this)

val Int.pctVolume
    get() = VolumeLevel.from(this.toDouble())
