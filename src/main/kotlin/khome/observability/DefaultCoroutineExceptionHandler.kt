package khome.observability

import khome.core.ConfigurationInterface
import khome.core.koin.KhomeKoinComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import mu.KotlinLogging
import org.koin.core.inject
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal class DefaultEntityObserverExceptionHandler : AbstractCoroutineContextElement(CoroutineExceptionHandler),
    EntityObserverExceptionHandler, KhomeKoinComponent {
    private val config: ConfigurationInterface by inject()
    private val logger = KotlinLogging.logger { }
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger.error(exception) { "Caught Exception observer}" }
    }
}

interface EntityObserverExceptionHandler : CoroutineExceptionHandler
