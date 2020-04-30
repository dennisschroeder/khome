package khome.listening

import khome.core.events.EventInterface
import mu.KotlinLogging

class LifeCycleHandler(
    private val handle: String,
    private val event: EventInterface<*>
) {
    private val logger = KotlinLogging.logger { }

    fun disable() =
        event.unsubscribe(handle).also {
            logger.info { "Listener with handle: \"$handle\" disabled." }
        }
}
