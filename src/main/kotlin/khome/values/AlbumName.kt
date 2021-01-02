@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class AlbumName private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<AlbumName> {
        override fun <P> from(value: P): AlbumName {
            return AlbumName(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: AlbumName): P {
            return value.value as P
        }
    }
}

val String.albumName
    get() = AlbumName.from(this)

val Enum<*>.albumName
    get() = AlbumName.from(this.name)
