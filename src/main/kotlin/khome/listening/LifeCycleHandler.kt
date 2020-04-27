package khome.listening

import io.ktor.util.KtorExperimentalAPI
import khome.core.LifeCycleHandlerInterface
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.events.StateChangeEvent
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import org.koin.core.inject

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class LifeCycleHandler(val handle: String) : LifeCycleHandlerInterface, KhomeKoinComponent() {
    private val logger = KotlinLogging.logger { }
    private val stateChangeEvent: StateChangeEvent by inject()
    override fun cancel() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    fun disable() = stateChangeEvent.unsubscribe(handle).also {
        logger.info { "Callback with handle: \"$handle\" disabled." }
    }
}
