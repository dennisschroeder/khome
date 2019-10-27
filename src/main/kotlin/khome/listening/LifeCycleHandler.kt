package khome.listening

import io.ktor.util.KtorExperimentalAPI
import khome.core.entities.EntityInterface
import khome.core.LifeCycleHandlerInterface
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.eventHandling.StateChangeEvents
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.inject

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class LifeCycleHandler(val handle: String, val entityId: EntityInterface) : LifeCycleHandlerInterface, KhomeKoinComponent() {
    private val stateChangeEvents: StateChangeEvents by inject()
    override fun cancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun disable() = stateChangeEvents.unsubscribe(handle)
}
