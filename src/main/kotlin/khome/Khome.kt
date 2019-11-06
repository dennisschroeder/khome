package khome

import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.send
import io.ktor.util.KtorExperimentalAPI
import khome.calling.FetchServices
import khome.calling.FetchStates
import khome.core.ConfigurationInterface
import khome.core.ErrorResult
import khome.core.EventResult
import khome.core.ListenEvent
import khome.core.Result
import khome.core.ServiceResult
import khome.core.ServiceStoreInterface
import khome.core.StateResult
import khome.core.StateStoreInterface
import khome.core.authenticate
import khome.core.dependencyInjection.CallerID
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.dependencyInjection.KhomeKoinContext
import khome.core.dependencyInjection.get
import khome.core.dependencyInjection.inject
import khome.core.dependencyInjection.loadKhomeModule
import khome.core.eventHandling.FailureResponseEvents
import khome.core.eventHandling.StateChangeEvents
import khome.core.eventHandling.SuccessResponseEvents
import khome.core.exceptions.EventStreamException
import khome.core.logger
import khome.core.toObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The main entry point to start your application
 *
 * @param init The type safe builder function to access the receiver
 * @return instance of Khome class instantiated with default values.
 */

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun khomeApplication(init: Khome.() -> Unit): Khome {
    KhomeKoinContext.startKoinApplication()
    return Khome().apply(init)
}

/**
 * The main application Class.
 * Serves all the tools necessary for the application to run.
 *
 * @author Dennis Schröder
 */
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
class Khome : KhomeKoinComponent() {
    companion object {
        private var sandboxMode = AtomicBoolean(false)

        val isSandBoxModeActive get() = sandboxMode.get()
        private fun activateSandBoxMode() = sandboxMode.set(true)
        private fun deactivateSandBoxMode() = sandboxMode.set(false)
    }

    /**
     * Configure your Khome instance. See all available properties in
     * the [ConfigurationInterface] data class.
     *
     * @param init Lambda with receiver to configure Khome
     * @see [ConfigurationInterface]
     */
    fun configure(init: ConfigurationInterface.() -> Unit) {
        val config: ConfigurationInterface by inject()
        config.apply(init)
    }

    fun beans(beanDeclarations: Module.() -> Unit) =
        loadKhomeModule(module(override = true, moduleDeclaration = beanDeclarations))

    /**
     * The connect function is the window to your home assistant instance.
     * Basically it is an wrapper of the ktor websocket client function.
     * Inside the lambda, that you have to pass in, you can register
     * state change based or time change based callbacks, call external api´s , or do whatever you`l like.
     *
     * @see khome.listening.onStateChange
     * @see khome.scheduling
     *
     */
    @ExperimentalCoroutinesApi
    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    suspend fun connectAndRun(listeners: suspend DefaultClientWebSocketSession.() -> Unit) =
        coroutineScope {
            get<KhomeClient>()
                .startSession {
                    configureLogger(get())
                    runApplication(get(), listeners)
                }
        }
}

private fun DefaultClientWebSocketSession.configureLogger(config: ConfigurationInterface) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, config.logLevel)
    System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_DATE_TIME_KEY, "${config.logTime}")
    System.setProperty(org.slf4j.impl.SimpleLogger.DATE_TIME_FORMAT_KEY, config.logTimeFormat)
    System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, config.logOutput)
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
private suspend fun DefaultClientWebSocketSession.runApplication(
    config: ConfigurationInterface,
    listeners: suspend DefaultClientWebSocketSession.() -> Unit
) {
    authenticate(get())

    fetchServices(get())
    storeServices(consumeMessage(), get())

    if (config.startStateStream) {
        fetchStates(get())
        storeStates(consumeMessage(), get())
        subscribeStateChanges(get())
    }

    listeners()

    if (successfullyStartedStateStream()) {
        consumeStateChangesByTriggeringEvents()
    } else
        throw EventStreamException("Could not subscribe to event stream!")
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
private suspend fun DefaultClientWebSocketSession.consumeStateChangesByTriggeringEvents() = coroutineScope {
    val stateChangeEvents: StateChangeEvents by inject()

    incoming.consumeEach { frame ->
        val message = frame.asObject<Map<String, Any>>()
        val type = message["type"]

        when (type) {
            "event" -> {
                updateLocalStateStore(frame, get())
                stateChangeEvents.emit(frame.asObject())
            }
            "result" -> {
                resolveResultTypeAndEmitEvents(frame)
            }
            else -> logger.warn { "Could not classify message: $type" }
        }
    }
}

private fun DefaultClientWebSocketSession.resolveResultTypeAndEmitEvents(frame: Frame) {
    val resultData = frame.asObject<Result>()
    logger.debug { "Result: $resultData" }
    when {
        !resultData.success -> emitResultErrorEventAndPrintLogMessage(resultData)
        resultData.success && resultData.result is ArrayList<*> -> checkLocalStateStoreAndRefresh(frame)
        resultData.success -> {
            val successResponseEvents: SuccessResponseEvents by inject()
            successResponseEvents.emit(frame.asObject())
            logResults(resultData)
        }
    }
}

private fun DefaultClientWebSocketSession.checkLocalStateStoreAndRefresh(frame: Frame) {
    val states = frame.asObject<StateResult>()
    var noneEqualStateCount = 0
    val stateStore: StateStoreInterface by inject()

    logger.info { " ###    Started local state store check    ###" }

    states.result.forEach { state ->

        if (stateStore[state.entityId] != state) {
            noneEqualStateCount++
            logger.warn { "The state of ${state.entityId} is not in sync any more." }
            stateStore[state.entityId] = state
            logger.warn { "The state has been refreshed." }
        }
    }

    if (noneEqualStateCount > 0) logger.debug { """--- $noneEqualStateCount none equal states discovered --- """ }
    logger.info { " ###    Local state store check finished    ###" }
}

private fun DefaultClientWebSocketSession.logResults(resultData: Result) =
    logger.info { "Result-Id: ${resultData.id} | Success: ${resultData.success}" }

private fun DefaultClientWebSocketSession.emitResultErrorEventAndPrintLogMessage(resultData: Result) {
    val errorCode = resultData.error?.let { it["code"] }!!
    val errorMessage = resultData.error.let { it["message"] }!!

    val failureResponseEvents: FailureResponseEvents by inject()
    failureResponseEvents.emit(ErrorResult(errorCode, errorMessage))
    logger.error { "$errorCode: $errorMessage" }
}

private fun DefaultClientWebSocketSession.updateLocalStateStore(frame: Frame, stateStore: StateStoreInterface) {
    val data = frame.asObject<EventResult>()
    data.event.data.newState?.let { stateStore[data.event.data.entityId] = it }
}

internal suspend fun DefaultClientWebSocketSession.fetchServices(id: CallerID) {
    val payload = FetchServices(id.incrementAndGet())
    callWebSocketApi(payload.toJson())
}

internal fun DefaultClientWebSocketSession.storeServices(
    serviceResult: ServiceResult,
    serviceStore: ServiceStoreInterface
) =
    serviceResult
        .result
        .forEach { (domain, services) ->
            services.forEach { (name, _) ->
                serviceStore[domain] = listOf(name)
                logger.debug { "Fetched service: $name from domain: $domain" }
            }
        }

internal suspend fun DefaultClientWebSocketSession.subscribeStateChanges(id: CallerID) =
    callWebSocketApi(ListenEvent(id.incrementAndGet(), eventType = "state_changed").toJson())

internal fun DefaultClientWebSocketSession.storeStates(stateResults: StateResult, stateStore: StateStoreInterface) =
    stateResults.result
        .forEach { state ->
            stateStore[state.entityId] = state
            logger.debug { "Fetched state with data: ${stateStore[state.entityId]}" }
        }

internal suspend fun DefaultClientWebSocketSession.fetchStates(id: CallerID) =
    callWebSocketApi(FetchStates(id.incrementAndGet()).toJson())

suspend fun DefaultClientWebSocketSession.callWebSocketApi(content: String) = send(content)

private suspend fun DefaultClientWebSocketSession.successfullyStartedStateStream() = consumeMessage<Result>().success

internal suspend inline fun <reified M : Any> DefaultClientWebSocketSession.consumeMessage(): M =
    incoming.receive().asObject()

internal inline fun <reified M : Any> Frame.asObject() = (this as Frame.Text).toObject<M>()
