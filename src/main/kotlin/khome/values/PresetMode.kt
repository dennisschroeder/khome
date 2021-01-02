@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class PresetMode private constructor(val value: String) {
    override fun toString(): String = value

    val isNone
        get() = value == "none"

    companion object : KhomeTypeAdapter<PresetMode> {
        override fun <P> from(value: P): PresetMode {
            return PresetMode(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: PresetMode): P {
            return value.value as P
        }
    }
}

val String.presetMode
    get() = PresetMode.from(this)
