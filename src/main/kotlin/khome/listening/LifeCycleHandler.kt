package khome.listening

import khome.Khome
import khome.core.LifeCycleHandlerInterface
import khome.core.logger
import khome.scheduling.runOnceInMinutes
import khome.scheduling.runOnceInSeconds

class LifeCycleHandler(handle: String, entityId: String) : LifeCycleHandlerInterface {
    override val lazyCancellation: Unit by lazy {
        Khome.stateChangeEvents.minusAssign(handle)
        logger.info { "Subscription to $entityId canceled." }
    }

    fun disable() = lazyCancellation
    fun disableInSeconds(seconds: Int) = runOnceInSeconds(seconds) { lazyCancellation }
    fun disableInMinutes(minutes: Int) = runOnceInMinutes(minutes) { lazyCancellation }
}