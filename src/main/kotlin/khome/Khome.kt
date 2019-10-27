package khome

import khome.core.*
import kotlinx.coroutines.*
import khome.calling.FetchStates
import khome.calling.FetchServices
import kotlin.collections.ArrayList
import io.ktor.http.cio.websocket.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.client.features.websocket.*
import kotlinx.coroutines.channels.consumeEach
import java.util.concurrent.atomic.AtomicBoolean
import khome.core.exceptions.EventStreamException
import khome.core.dependencyInjection.*
import khome.core.dependencyInjection.CallerID
import khome.core.eventHandling.FailureResponseEvents
import khome.core.eventHandling.StateChangeEvents
import khome.core.eventHandling.SuccessResponseEvents
import org.koin.core.inject
import org.koin.core.module.Module
import org.koin.dsl.module

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
     * the [Configuration] data class.
     *
     * @param init Lamba with receiver to configure Khome
     * @see Configuration
     */
    fun configure(init: ConfigurationInterface.() -> Unit) {
        val config : ConfigurationInterface by inject()
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
    suspend fun connectAndRun(reactOnStateChangeEvents: suspend DefaultClientWebSocketSession.() -> Unit) =
        coroutineScope {
            val client: KhomeClient by inject()
            client.startSession {
                configureLogger(get())
                runApplication(get(), reactOnStateChangeEvents)
            }
        }
}

private fun DefaultClientWebSocketSession.configureLogger(config: ConfigurationInterface) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, config.logLevel)
    System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_DATE_TIME_KEY, "${config.logTime}")
    System.setProperty(org.slf4j.impl.SimpleLogger.DATE_TIME_FORMAT_KEY, config.logTimeFormat)
    System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, config.logOutput)
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
private suspend fun DefaultClientWebSocketSession.runApplication(
    config: ConfigurationInterface,
    reactOnStateChangeEvents: suspend DefaultClientWebSocketSession.() -> Unit
) {
    authenticate(get())
    fetchAvailableServicesFromApi()

    if (config.startStateStream)
        startStateStream()

    reactOnStateChangeEvents()

    if (successfullyStartedStateStream()) {
        val stateChangeEvents: StateChangeEvents by inject()
        logger.debug { "${stateChangeEvents.count()} callbacks registered" }
        consumeStateChangesByTriggeringEvents()
    } else
        throw EventStreamException("Could not subscribe to event stream!")
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
private suspend fun DefaultClientWebSocketSession.consumeStateChangesByTriggeringEvents() = coroutineScope {
    incoming.consumeEach { frame ->
        val message = frame.asObject<Map<String, Any>>()
        val type = message["type"]
        val stateChangeEvents: StateChangeEvents by inject()

        when (type) {
            "event" -> {
                updateLocalStateStore(frame)
                stateChangeEvents.emit(frame.asObject())
                logger.debug { "${stateChangeEvents.count()} callbacks registered" }
                logger.debug {
                    """
                            ${frame.asObject<EventResult>().event.data.entityId}: OldState: ${frame.asObject<EventResult>().event.data.oldState.getValue<Any>()} || NewState: ${frame.asObject<EventResult>().event.data.newState.getValue<Any>()}
                            """.trimIndent()
                }
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
    val stateStore: StateStore by inject()

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
    val errorCode = resultData.error?.get("code")!!
    val errorMessage = resultData.error.get("message")!!

    val failureResponseEvents: FailureResponseEvents by inject()
    failureResponseEvents.emit(ErrorResult(errorCode, errorMessage))
    logger.error { "$errorCode: $errorMessage" }
}

private suspend fun DefaultClientWebSocketSession.updateLocalStateStore(frame: Frame) {
    val data = frame.asObject<EventResult>()
    val states: StateStore by inject()
    if (states[data.event.data.entityId] == data.event.data.newState) fetchStates()
    else states[data.event.data.entityId] = data.event.data.newState
}

private suspend fun DefaultClientWebSocketSession.fetchAvailableServicesFromApi() {
    val callerId: CallerID by inject()
    val payload = FetchServices(callerId.incrementAndGet())
    callWebSocketApi(payload.toJson())

    val serviceStore: ServiceStore by inject()
    consumeMessage<ServiceResult>()
        .result
        .forEach { (domain, services) ->
            services.forEach { (name, _) ->
                serviceStore[domain] = listOf(name)
                logger.debug { "Fetched service: $name from domain: $domain" }
            }
        }
}

private suspend fun DefaultClientWebSocketSession.startStateStream() {
    fetchStates()
    val states: StateStore by inject()
    consumeMessage<StateResult>()
        .result
        .forEach { state ->
            states[state.entityId] = state
            logger.debug { "Fetched state with data: ${states[state.entityId]}" }
        }

    val callerId: CallerID by inject()
    callWebSocketApi(ListenEvent(callerId.incrementAndGet(), eventType = "state_changed").toJson())
}

private suspend fun DefaultClientWebSocketSession.fetchStates() =
    callWebSocketApi(FetchStates(get<CallerID>().incrementAndGet()).toJson())

suspend fun DefaultClientWebSocketSession.callWebSocketApi(content: String) = send(content)

private suspend fun DefaultClientWebSocketSession.successfullyStartedStateStream() = consumeMessage<Result>().success

internal suspend inline fun <reified M : Any> DefaultClientWebSocketSession.consumeMessage(): M =
    incoming.receive().asObject()

internal inline fun <reified M : Any> Frame.asObject() = (this as Frame.Text).toObject<M>()
