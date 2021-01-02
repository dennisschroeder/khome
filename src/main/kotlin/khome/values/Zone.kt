package khome.values

import khome.core.mapping.KhomeTypeAdapter

@Suppress("DataClassPrivateConstructor")
data class Zone private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<Zone> {
        override fun <P> from(value: P): Zone {
            return Zone(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Zone): P {
            return value.value as P
        }
    }
}

val String.zone
    get() = Zone.from(this)

val Enum<*>.zone
    get() = Zone.from(this.name)
