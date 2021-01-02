@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Artist private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<Artist> {
        override fun <P> from(value: P): Artist {
            return Artist(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Artist): P {
            return value.value as P
        }
    }
}

val String.artist
    get() = Artist.from(this)

val Enum<*>.artist
    get() = Artist.from(this.name)
