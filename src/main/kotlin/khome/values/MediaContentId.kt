@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class MediaContentId private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<MediaContentId> {
        override fun <P> from(value: P): MediaContentId {
            return MediaContentId(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: MediaContentId): P {
            return value.value as P
        }
    }
}
