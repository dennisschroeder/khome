@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class MediaTitle private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<MediaTitle> {
        override fun <P> from(value: P): MediaTitle {
            return MediaTitle(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: MediaTitle): P {
            return value.value as P
        }
    }
}
