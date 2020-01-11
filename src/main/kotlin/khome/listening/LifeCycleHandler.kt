package khome.listening

import io.ktor.util.KtorExperimentalAPI
import khome.core.LifeCycleHandlerInterface
import khome.core.dependencyInjection.KhomeComponent
import khome.core.entities.EntityInterface
import khome.core.eventHandling.StateChangeEvent
import khome.core.logger
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.inject

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class LifeCycleHandler(val handle: String, val entityId: EntityInterface) : LifeCycleHandlerInterface, KhomeComponent() {
    private val stateChangeEvent: StateChangeEvent by inject()
    override fun cancel() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    fun disable() = stateChangeEvent.unsubscribe(handle).also {
        logger.info { "Callback with handle: \"$handle\" disabled." }
    }
}
