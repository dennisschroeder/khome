package khome.values

import khome.core.mapping.KhomeTypeAdapter

@Suppress("DataClassPrivateConstructor")
data class Domain private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<Domain> {
        override fun <P> from(value: P): Domain {
            return Domain(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Domain): P {
            return value.toString() as P
        }
    }
}

val String.domain
    get() = Domain.from(this)

val Enum<*>.domain
    get() = Domain.from(this.name)
