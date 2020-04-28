package khome.core.events

import khome.calling.PersistentNotificationCreate
import khome.calling.callService
import khome.core.BaseKhomeComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import mu.KotlinLogging
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class DefaultEventListenerExceptionHandler(private val baseKhomeComponent: BaseKhomeComponent) :
    AbstractCoroutineContextElement(CoroutineExceptionHandler), EventListenerExceptionHandler {

    private val logger = KotlinLogging.logger { }
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger.error(exception) { "Caught Exception in listener ${context[Job]}" }
        baseKhomeComponent.callService<PersistentNotificationCreate> {
            configure {
                title = "Error in Khome application."
                message = """
                    
                """.trimIndent()
            }
        }
    }
}
