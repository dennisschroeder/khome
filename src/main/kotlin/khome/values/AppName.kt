@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class AppName private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<AppName> {
        override fun <P> from(value: P): AppName {
            return AppName(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: AppName): P {
            return value.value as P
        }
    }
}

val String.appName
    get() = AppName.from(this)

val Enum<*>.appName
    get() = AppName.from(this.name)
