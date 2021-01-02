@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class EntityPicture private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<EntityPicture> {
        override fun <P> from(value: P): EntityPicture {
            return EntityPicture(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: EntityPicture): P {
            return value.value as P
        }
    }
}
