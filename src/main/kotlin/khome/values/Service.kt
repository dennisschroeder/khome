@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class Service private constructor(val value: String) {
    companion object : KhomeTypeAdapter<Service> {

        fun fromDevice(device: Device): Service =
            Service(device.value)

        override fun <P> from(value: P): Service {
            return Service(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: Service): P {
            return value.value as P
        }
    }
}

val String.service
    get() = Service.from(this)
