package khome.values

import khome.core.mapping.KhomeTypeAdapter

@Suppress("DataClassPrivateConstructor")
data class ObjectId private constructor(val value: String) {

    override fun toString(): String = value

    companion object : KhomeTypeAdapter<ObjectId> {
        override fun <P> from(value: P): ObjectId {
            return ObjectId(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: ObjectId): P {
            return value.value as P
        }
    }
}

val String.objectId
    get() = ObjectId.from(this)
