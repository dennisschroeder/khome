@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class AppId private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<AppId> {
        override fun <P> from(value: P): AppId {
            return AppId(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: AppId): P {
            return value.value as P
        }
    }
}

val String.appId
    get() = AppId.from(this)

val Enum<*>.appId
    get() = AppId.from(this.name)
