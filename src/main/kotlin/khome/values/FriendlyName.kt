@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class FriendlyName private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<FriendlyName> {
        override fun <P> from(value: P): FriendlyName {
            return FriendlyName(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: FriendlyName): P {
            return value.value as P
        }
    }
}
