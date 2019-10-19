package khome

import khome.core.*
import kotlinx.coroutines.*
import io.ktor.http.HttpMethod
import khome.calling.FetchStates
import io.ktor.client.HttpClient
import khome.calling.FetchServices
import kotlin.collections.ArrayList
import io.ktor.http.cio.websocket.*
import khome.Khome.Companion.states
import io.ktor.client.engine.cio.CIO
import khome.Khome.Companion.idCounter
import io.ktor.util.KtorExperimentalAPI
import io.ktor.client.features.websocket.*
import khome.Khome.Companion.emitResultEvent
import kotlinx.coroutines.channels.consumeEach
import khome.Khome.Companion.stateListenerCount
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import khome.core.exceptions.EventStreamException
import khome.Khome.Companion.emitErrorResultEvent
import khome.Khome.Companion.emitStateChangeEvent
import khome.Khome.Companion.koinApp
import khome.Khome.Companion.services
import khome.calling.Exceptions.DomainNotFoundException
import khome.calling.ServiceCaller
import org.koin.core.KoinApplication
import org.koin.core.logger.Level
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/**
 * The main entry point to start your application
 *
 * @param init The type safe builder function to access the receiver
 * @return instance of Khome class instantiated with default values.
 */
fun initialize(init: Khome.() -> Unit): Khome {
    return Khome().apply(init)
}

/**
 * The main application Class.
 * Serves all the tools necessary for the application to run.
 *
 * @author Dennis Schröder
 */
class Khome {
    companion object {
        private var dirty = false
        internal val states = hashMapOf<String, State>()
        internal val services = hashMapOf<String, List<String>>()

        /**
         * The Khome encapsulated Koin context
         * https://insert-koin.io/docs/2.0/documentation/reference/index.html#_koin_context_isolation
         */

        internal var koinApp: KoinApplication? = null
        internal var entitiesModule: Module? = null

        /**
         * STATE CHANGE EVENTS
         */

        private val stateChangeEvents = Event<EventResult>()

        internal val stateListenerCount get() = stateChangeEvents.listeners.size

        internal fun subscribeStateChangeEvent(handle: String? = null, callback: EventResult.() -> Unit) {
            if (handle == null)
                stateChangeEvents += callback
            else
                stateChangeEvents[handle] = callback
        }

        internal fun unsubscribeStateChangeEvent(handle: String) = stateChangeEvents.minus(handle)

        internal fun emitStateChangeEvent(eventData: EventResult) = stateChangeEvents(eventData)

        /**
         * SCHEDULER CANCEL EVENTS
         */
        private val schedulerCancelEvents = Event<String>()

        internal fun subscribeSchedulerCancelEvents(handle: String? = null, callback: String.() -> Unit) {
            if (handle == null)
                schedulerCancelEvents += callback
            else
                schedulerCancelEvents[handle] = callback
        }

        internal fun unsubscribeSchedulerCancelEvents(handle: String) = schedulerCancelEvents.minus(handle)

        internal fun emitSchedulerCancelEvents(eventData: String) = schedulerCancelEvents(eventData)

        /**
         * RESULT EVENTS
         */
        private val resultEvents = Event<Result>()

        internal fun subscribeResultEvent(handle: String? = null, callback: Result.() -> Unit) {
            if (handle == null)
                resultEvents += callback
            else
                resultEvents[handle] = callback
        }

        internal fun unsubscribeResultEvent(handle: String) = resultEvents.minusAssign(handle)

        internal fun emitResultEvent(eventData: Result) = resultEvents(eventData)

        /**
         * ERROR RESULT EVENTS
         */
        private val errorResultEvents = Event<ErrorResult>()

        internal fun subscribeErrorResultEvent(handle: String? = null, callback: ErrorResult.() -> Unit) {
            if (handle == null)
                errorResultEvents += callback
            else
                errorResultEvents[handle] = callback
        }

        internal fun unsubscribeErrorResultEvent(handle: String) = errorResultEvents.minusAssign(handle)

        internal fun emitErrorResultEvent(eventData: ErrorResult) = errorResultEvents(eventData)

        private val config = Configuration()

        private fun resetApplicationState() {
            states.clear()
            services.clear()
            resultEvents.clear()
            errorResultEvents.clear()
            stateChangeEvents.clear()
            schedulerCancelEvents.clear()
            deactivateSandBoxMode()
        }

        private fun cancelAllScheduledCallbacks() =
            schedulerCancelEvents
                .listeners
                .forEach { action ->
                    action.value.invoke("Cancel all scheduled callbacks")

                }

        internal fun reconnect() {
            logger.debug { "Reconnecting and therefore resetting the application state" }
            dirty = true
            resetApplicationState()
            cancelAllScheduledCallbacks()
        }

        /**
         * Since the Homeassistant web socket API needs an incrementing
         * id when calling it, we need to provide the callService feature
         * with such an id.
         *
         * @see "https://developers.home-assistant.io/docs/en/external_api_websocket.html#message-format"
         */
        internal var idCounter = AtomicInteger(0)

        private var sandboxMode = AtomicBoolean(false)

        internal val isSandBoxModeActive get() = sandboxMode.get()
        private fun activateSandBoxMode() = sandboxMode.set(true)
        private fun deactivateSandBoxMode() = sandboxMode.set(false)

        /**
         *  A single thread context needed to run all websocket api calls in.
         *  Since api calls has to have an incrementing id, it is necessary to make
         *  the calls threadsafe.
         *  @see khome.calling.callService
         */
        @ObsoleteCoroutinesApi
        internal val callServiceContext = newSingleThreadContext("ServiceContext")
    }

    private val method = HttpMethod.Get
    private val path = "/api/websocket"

    /**
     * Configure your Khome instance. See all available properties in
     * the [Configuration] data class.
     *
     * @param init Lamba with receiver to configure Khome
     * @see Configuration
     */
    fun configure(init: Configuration.() -> Unit) {
        config.apply(init)

        System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, config.logLevel)
        System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_DATE_TIME_KEY, "${config.logTime}")
        System.setProperty(org.slf4j.impl.SimpleLogger.DATE_TIME_FORMAT_KEY, config.logTimeFormat)
        System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, config.logOutput)
    }

    fun declareEntities(entityDeclarations: Module.() -> Unit) =
        module(createdAtStart = true, moduleDeclaration = entityDeclarations).let { entitiesModule = it }

    @KtorExperimentalAPI
    private val client = HttpClient(CIO).config {
        install(WebSockets)
    }

    /**
     * The connect function is the window to your home assistant instance.
     * Basically it is an wrapper of the ktor websocket client function.
     * Inside the lambda, that you have to pass in, you can register
     * state change based or time change based callbacks, call external api´s , or do whatever you`l like.
     *
     * @see khome.listening.listenState
     * @see khome.scheduling
     *
     */
    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    suspend fun connect(reactOnStateChangeEvents: suspend DefaultClientWebSocketSession.() -> Unit) =
        coroutineScope {
            if (config.secure)
                client.wss(
                    method = method,
                    host = config.host,
                    port = config.port,
                    path = path
                ) { runApplication(config, reactOnStateChangeEvents) }
            else
                client.ws(
                    method = method,
                    host = config.host,
                    port = config.port,
                    path = path
                ) { runApplication(config, reactOnStateChangeEvents) }
        }
}

@ObsoleteCoroutinesApi
private suspend fun DefaultClientWebSocketSession.runApplication(
    config: Configuration,
    reactOnStateChangeEvents: suspend DefaultClientWebSocketSession.() -> Unit
) {
    authenticate(config.accessToken)
    fetchAvailableServicesFromApi()

    if (config.startStateStream)
        startStateStream()

    koinApp = koinApplication {
        printLogger(Level.DEBUG)
        Khome.entitiesModule?.let { modules(it) }
    }

    reactOnStateChangeEvents()

    if (successfullyStartedStateStream()) {
        logger.debug { "$stateListenerCount callbacks registered" }
        consumeStateChangesByTriggeringEvents()
    } else
        throw EventStreamException("Could not subscribe to event stream!")
}

@ObsoleteCoroutinesApi
private suspend fun WebSocketSession.consumeStateChangesByTriggeringEvents() = coroutineScope {
    incoming.consumeEach { frame ->
        val message = frame.asObject<Map<String, Any>>()
        val type = message["type"]

        when (type) {
            "event" -> {
                updateLocalStateStore(frame)
                emitStateChangeEvent(frame.asObject())
                logger.debug { "$stateListenerCount callbacks registered" }
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

private fun resolveResultTypeAndEmitEvents(frame: Frame) {
    val resultData = frame.asObject<Result>()
    logger.debug { "Result: $resultData" }
    when {
        !resultData.success -> emitResultErrorEventAndPrintLogMessage(resultData)
        resultData.success && resultData.result is ArrayList<*> -> checkLocalStateStoreAndRefresh(frame)
        resultData.success -> {
            emitResultEvent(frame.asObject())
            logResults(resultData)
        }
    }
}

private fun checkLocalStateStoreAndRefresh(frame: Frame) {
    val states = frame.asObject<StateResult>()
    var noneEqualStateCount = 0

    logger.info { " ###    Started local state store check    ###" }

    states.result.forEach { state ->
        if (Khome.states[state.entityId] != state) {
            noneEqualStateCount++
            logger.warn { "The state of ${state.entityId} is not in sync any more." }
            Khome.states[state.entityId] = state
            logger.warn { "The state has been refreshed." }
        }
    }

    if (noneEqualStateCount > 0) logger.debug { """--- $noneEqualStateCount none equal states discovered --- """ }
    logger.info { " ###    Local state store check finished    ###" }

}

private fun logResults(resultData: Result) =
    logger.info { "Result-Id: ${resultData.id} | Success: ${resultData.success}" }


private fun emitResultErrorEventAndPrintLogMessage(resultData: Result) {
    val errorCode = resultData.error?.get("code")!!
    val errorMessage = resultData.error.get("message")!!

    emitErrorResultEvent(ErrorResult(errorCode, errorMessage))
    logger.error { "$errorCode: $errorMessage" }
}

private suspend fun WebSocketSession.updateLocalStateStore(frame: Frame) {
    val data = frame.asObject<EventResult>()
    if (states[data.event.data.entityId] == data.event.data.newState) fetchStates()
    else states[data.event.data.entityId] = data.event.data.newState
}

private suspend fun WebSocketSession.fetchAvailableServicesFromApi() {
    val payload = FetchServices(idCounter.incrementAndGet())
    callWebSocketApi(payload.toJson())

    consumeMessage<ServiceResult>()
        .result
        .forEach { (domain, services) ->
            val serviceCollection = mutableListOf<String>()
            services.forEach { (name, _) ->
                serviceCollection.add(name)
                logger.debug { "Fetched service: $name from domain: $domain" }
            }
            Khome.services[domain] = serviceCollection
        }
}

private suspend fun WebSocketSession.startStateStream() {
    fetchStates()
    consumeMessage<StateResult>()
        .result
        .forEach { state ->
            states[state.entityId] = state
            logger.debug { "Fetched state with data: $state" }
        }

    callWebSocketApi(ListenEvent(idCounter.incrementAndGet(), eventType = "state_changed").toJson())
}

private suspend fun WebSocketSession.fetchStates() = callWebSocketApi(FetchStates(idCounter.incrementAndGet()).toJson())

internal suspend fun WebSocketSession.callWebSocketApi(content: String) = send(content)

private suspend fun WebSocketSession.successfullyStartedStateStream() = consumeMessage<Result>().success

internal suspend inline fun <reified M : Any> WebSocketSession.consumeMessage(): M = incoming.receive().asObject()

internal inline fun <reified M : Any> Frame.asObject() = (this as Frame.Text).toObject<M>()
