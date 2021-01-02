@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class MediaPosition private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<MediaPosition> {
        override fun <P> from(value: P): MediaPosition {
            return MediaPosition((value as Double))
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: MediaPosition): P {
            return value.value as P
        }
    }
}

val Double.position
    get() = MediaPosition.from(this)

val Int.position
    get() = MediaPosition.from(this)
