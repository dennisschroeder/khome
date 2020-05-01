package khome.core.events

import khome.calling.PersistentNotificationCreate
import khome.calling.callService
import khome.core.BaseKhomeComponent
import khome.core.ConfigurationInterface
import khome.core.ErrorResponseListenerContext
import khome.listening.HassEventListenerContext
import khome.listening.StateListenerContext
import kotlinx.coroutines.CoroutineExceptionHandler
import mu.KotlinLogging
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class DefaultStateChangeListenerExceptionHandler(
    private val baseKhomeComponent: BaseKhomeComponent,
    private val configurationInterface: ConfigurationInterface
) : AbstractCoroutineContextElement(CoroutineExceptionHandler), StateChangeListenerExceptionHandler {

    private val logger = KotlinLogging.logger { }
    override fun handleException(context: CoroutineContext, exception: Throwable) =
        context[StateListenerContext]?.let { stateListenerContext ->
            logger.error(exception) { "Caught Exception in listener of entity: ${stateListenerContext.entityId} with handle: ${stateListenerContext.handle}" }
            baseKhomeComponent.callService<PersistentNotificationCreate> {
                configure {
                    notificationId = stateListenerContext.entityId
                    title = "Error in Khome application: ${configurationInterface.name}"
                    message = """
                        Caught Exception in listener of entity: **${stateListenerContext.entityId}** 
                        with handle: ${stateListenerContext.handle}
                        $exception
                        ${exception.stackTrace.first()}
                    """.trimIndent()
                }
            }
            stateListenerContext.lifeCycleHandler.disable()
        } ?: throw IllegalStateException("No StateListenerContext in coroutine context.")
}

class DefaultHassEventListenerExceptionHandler(
    private val baseKhomeComponent: BaseKhomeComponent,
    private val configurationInterface: ConfigurationInterface
) : AbstractCoroutineContextElement(CoroutineExceptionHandler), EventListenerExceptionHandler {

    private val logger = KotlinLogging.logger { }
    override fun handleException(context: CoroutineContext, exception: Throwable) =
        context[HassEventListenerContext]?.let { hassEventListenerContext ->
            logger.error(exception) { "Caught Exception in listener of hassEvent: ${hassEventListenerContext.eventType} with handle: ${hassEventListenerContext.handle}" }
            baseKhomeComponent.callService<PersistentNotificationCreate> {
                configure {
                    notificationId = hassEventListenerContext.eventType
                    title = "Error in Khome application: ${configurationInterface.name}"
                    message = """
                        Caught Exception in listener of hassEvent: **${hassEventListenerContext.eventType}** 
                        with handle: ${hassEventListenerContext.handle}
                        $exception
                        ${exception.stackTrace.first()}
                    """.trimIndent()
                }
            }
            hassEventListenerContext.lifeCycleHandler.disable()
        } ?: throw IllegalStateException("No HassEventListenerContext in coroutine context.")
}

class DefaultErrorResultListenerExceptionHandler(
    private val baseKhomeComponent: BaseKhomeComponent,
    private val configurationInterface: ConfigurationInterface
) : AbstractCoroutineContextElement(CoroutineExceptionHandler), ErrorResultListenerExceptionHandler {

    private val logger = KotlinLogging.logger { }
    override fun handleException(context: CoroutineContext, exception: Throwable) =
        context[ErrorResponseListenerContext]?.let { errorResponseContext ->
            logger.error(exception) { "Caught Exception in listener of error response with handle: ${errorResponseContext.handle}" }
            baseKhomeComponent.callService<PersistentNotificationCreate> {
                configure {
                    notificationId = "error_response"
                    title = "Error in Khome application: ${configurationInterface.name}"
                    message = """
                        Caught Exception in listener of **error response**
                        with handle: ${errorResponseContext.handle}
                        $exception
                        ${exception.stackTrace.first()}
                    """.trimIndent()
                }
            }
            errorResponseContext.lifeCycleHandler.disable()
        } ?: throw IllegalStateException("No ErrorResponseListenerContext in coroutine context.")
}

interface StateChangeListenerExceptionHandler : CoroutineExceptionHandler

interface EventListenerExceptionHandler : CoroutineExceptionHandler

interface ErrorResultListenerExceptionHandler : CoroutineExceptionHandler
