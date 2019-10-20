package khome.listening

import khome.core.logger
import khome.scheduling.runOnceInMinutes
import khome.scheduling.runOnceInSeconds
import khome.core.entities.EntityInterface
import khome.core.LifeCycleHandlerInterface
import khome.Khome.Companion.stateChangeEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LifeCycleHandler(val handle: String, val entityId: EntityInterface) : LifeCycleHandlerInterface, CoroutineScope by CoroutineScope(Dispatchers.Default) {
    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun disable() = stateChangeEvents.unsubscribe(handle)
    fun disableInSeconds(seconds: Long) = launch { runOnceInSeconds(seconds) { disable() } }
    fun disableInMinutes(minutes: Long) = launch { runOnceInMinutes(minutes) { disable() } }
}
