@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class MediaSource private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<MediaSource> {
        override fun <P> from(value: P): MediaSource {
            return MediaSource(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: MediaSource): P {
            return value.value as P
        }
    }
}

val String.mediaSource
    get() = MediaSource.from(this)

val Enum<*>.mediaSource
    get() = MediaSource.from(this)
