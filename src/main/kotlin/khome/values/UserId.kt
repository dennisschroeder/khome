@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class UserId private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<UserId> {
        override fun <P> from(value: P): UserId {
            return UserId(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: UserId): P {
            return value.value as P
        }
    }
}
