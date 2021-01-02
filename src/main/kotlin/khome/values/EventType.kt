@file:Suppress("DataClassPrivateConstructor")

package khome.values

import khome.core.mapping.KhomeTypeAdapter

data class EventType private constructor(val value: String) {
    override fun toString(): String = value

    companion object : KhomeTypeAdapter<EventType> {
        override fun <P> from(value: P): EventType {
            return EventType(value as String)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <P> to(value: EventType): P {
            return value.value as P
        }
    }
}

val String.eventType
    get() = EventType.from(this)

val Enum<*>.eventType
    get() = EventType.from(this.name)
