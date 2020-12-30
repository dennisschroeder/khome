@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class MediaDuration private constructor(val value: Double) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<MediaDuration> {
        override fun <P> from(value: P): MediaDuration {
            return MediaDuration((value as Double))
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: MediaDuration): P {
            return value.value as P
        }
    }
}
