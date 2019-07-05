package khome.listening

import khome.core.logger
import khome.scheduling.runOnceInMinutes
import khome.scheduling.runOnceInSeconds
import khome.core.entities.EntityInterface
import khome.core.LifeCycleHandlerInterface
import khome.Khome.Companion.unsubscribeStateChangeEvent

class LifeCycleHandler(handle: String, entityId: EntityInterface) : LifeCycleHandlerInterface {
    override val lazyCancellation: Unit by lazy {
        unsubscribeStateChangeEvent(handle)
        logger.info { "Subscription to ${entityId.id} disabled." }
    }

    fun disable() = lazyCancellation
    fun disableInSeconds(seconds: Int) = runOnceInSeconds(seconds) { lazyCancellation }
    fun disableInMinutes(minutes: Int) = runOnceInMinutes(minutes) { lazyCancellation }
}