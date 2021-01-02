@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Rising private constructor(val value: Boolean) {
    override fun toString(): String = value.toString()

    companion object : KhomeTypeAdapter<Rising> {
        override fun <P> from(value: P): Rising {
            return Rising((value as Boolean))
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Rising): P {
            return value.value as P
        }

        val TRUE get(): Rising = from(true)

        val FALSE get(): Rising = from(false)
    }
}
