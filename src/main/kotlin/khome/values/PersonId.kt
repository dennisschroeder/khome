@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class PersonId private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<PersonId> {
        override fun <P> from(value: P): PersonId {
            return PersonId(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: PersonId): P {
            return value.value as P
        }
    }
}
