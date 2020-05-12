package khome.core.events

import khome.calling.PersistentNotificationCreate
import khome.core.BaseKhomeComponent
import khome.core.ConfigurationInterface
import khome.core.ErrorResponseListenerContext
import khome.observing.AsyncStateObserverContext
import khome.observing.HassEventListenerContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

class DefaultEntityObserverExceptionHandler(
    private val baseKhomeComponent: BaseKhomeComponent,
    private val configurationInterface: ConfigurationInterface
) : AbstractCoroutineContextElement(CoroutineExceptionHandler), EntityObserverExceptionHandler {

    private val logger = KotlinLogging.logger { }
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        context[AsyncStateObserverContext]?.let { entityObservableContext ->
            logger.error(exception) { "Caught Exception in listener of entity: ${entityObservableContext.entity.entityId} with handle: ${entityObservableContext.handle}" }
            CoroutineScope(Dispatchers.IO).launch {
                logger.info { "In CoroutineScope" }
                baseKhomeComponent.callService<PersistentNotificationCreate> {
                    configure {
                        notificationId = entityObservableContext.entity.entityId.toString()
                        title = "Error in Khome application: ${configurationInterface.name}"
                        message = """
                            Caught Exception in listener of entity: **${entityObservableContext.entity.entityId}** 
                            with handle: ${entityObservableContext.handle}
                            $exception
                            ${exception.stackTrace.first()}
                        """.trimIndent()
                    }
                }
            }
            entityObservableContext.disableObservable()
        } ?: throw IllegalStateException("No StateListenerContext in coroutine context.")
    }
}

class DefaultHassEventListenerExceptionHandler(
    private val baseKhomeComponent: BaseKhomeComponent,
    private val configurationInterface: ConfigurationInterface
) : AbstractCoroutineContextElement(CoroutineExceptionHandler), EventListenerExceptionHandler {

    private val logger = KotlinLogging.logger { }
    override fun handleException(context: CoroutineContext, exception: Throwable) =
        context[HassEventListenerContext]?.let { hassEventListenerContext ->
            logger.error(exception) { "Caught Exception in listener of hassEvent: ${hassEventListenerContext.eventType} with handle: ${hassEventListenerContext.handle}" }
            CoroutineScope(Dispatchers.IO).launch {
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
            CoroutineScope(Dispatchers.IO).launch {
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
            }
            errorResponseContext.lifeCycleHandler.disable()
        } ?: throw IllegalStateException("No ErrorResponseListenerContext in coroutine context.")
}

interface EntityObserverExceptionHandler : CoroutineExceptionHandler

interface EventListenerExceptionHandler : CoroutineExceptionHandler

interface ErrorResultListenerExceptionHandler : CoroutineExceptionHandler
