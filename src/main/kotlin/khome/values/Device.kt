@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Device private constructor(val value: String) {
    override fun toString(): String = "$value.device"

    companion object : KhomeTypeAdapter<Device> {
        override fun <P> from(value: P): Device {
            return Device(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Device): P {
            return value.value as P
        }
    }
}

val String.device
    get() = Device.from(this)

val Enum<*>.device
    get() = Device.from(this.name)
