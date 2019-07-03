package khome

import khome.core.*
import kotlinx.coroutines.*
import java.time.LocalDateTime
import io.ktor.http.HttpMethod
import khome.scheduling.toDate
import kotlin.system.exitProcess
import io.ktor.client.HttpClient
import io.ktor.http.cio.websocket.*
import khome.Khome.Companion.states
import io.ktor.client.engine.cio.CIO
import khome.Khome.Companion.idCounter
import khome.Khome.Companion.reconnect
import io.ktor.util.KtorExperimentalAPI
import khome.Khome.Companion.resultEvents
import io.ktor.client.features.websocket.*
import khome.Khome.Companion.runInSandBoxMode
import khome.Khome.Companion.errorResultEvents
import khome.Khome.Companion.stateChangeEvents
import kotlinx.coroutines.channels.consumeEach
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import khome.Khome.Companion.schedulerTestEvents
import khome.core.exceptions.EventStreamException

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
 * Serves with all the tools necessary for the application to run.
 *
 * @author Dennis Schröder
 */
class Khome {
    companion object {
        /**
         * Indicates if a connection has been established.
         */
        var connected = false

        /**
         * The local state store. Serves as a cache for states.
         */
        val states = hashMapOf<String, State>()

        /**
         * A local list of available home assistant services. Needed for the integrity testing feature.
         */
        val services = hashMapOf<String, List<String>>()

        /**
         * List of registered callbacks triggered by state change events.
         */
        val stateChangeEvents = Event<EventResult>()

        /**
         * List of registered scheduler callbacks triggered only by the integrity testing feature.
         */
        val schedulerTestEvents = Event<String>()

        /**
         * List of registered scheduler-cancel callbacks only triggered by [reconnect] function.
         */
        val schedulerCancelEvents = Event<String>()

        /**
         * List of registered result callbacks triggered by result messages from the websocket api.
         */
        val resultEvents = Event<Result>()

        /**
         * List of registered error-result-callbacks triggered by error result messages from the websocket api.
         */
        val errorResultEvents = Event<ErrorResult>()

        private val config = Configuration()

        private fun resetApplicationState() {
            states.clear()
            services.clear()
            stateChangeEvents.clear()
            schedulerTestEvents.clear()
            schedulerCancelEvents.clear()
            resultEvents.clear()
            errorResultEvents.clear()
            deactivateSandBoxMode()
        }

        private fun cancelAllScheduledCallbacks() = schedulerCancelEvents("Restarted")

        /**
         * Reconnects to the home assistant websocket api
         */
        fun reconnect() {
            cancelAllScheduledCallbacks()
            resetApplicationState()
            connected = false
        }

        /**
         * Since the Homeassistant web socket API needs an incrementing
         * id when calling it, we need to provide the callService feature
         * with such an id.
         *
         * @see "https://developers.home-assistant.io/docs/en/external_api_websocket.html#message-format"
         */
        var idCounter = AtomicInteger(0)

        private var sandboxMode = AtomicBoolean(false)

        /**
         * In sandbox mode, all websocket api calls are intercepted.
         * This mode is needed by the integrity testing feature.
         */
        val isSandBoxModeActive get() = sandboxMode.get()
        private fun activateSandBoxMode() = sandboxMode.set(true)
        private fun deactivateSandBoxMode() = sandboxMode.set(false)
        fun runInSandBoxMode(action: () -> Unit) {
            activateSandBoxMode()
            action()
            deactivateSandBoxMode()
        }

        /**
         *  A single thread context needed to run all websocket api calls
         *  @see khome.calling.callService
         */
        @ObsoleteCoroutinesApi
        val callServiceContext = newSingleThreadContext("ServiceContext")
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

    @KtorExperimentalAPI
    private val client = HttpClient(CIO).config {
        install(WebSockets)
    }

    /**
     * The connect function is the window to your home assistant instance.
     * Basically it is an wrapper of the ktor websocket client function.
     * Inside the closure that you have to pass in the connect function, you can register
     * state change based or time based callbacks, call external api´s , or do whatever you`l like.
     *
     * @see khome.calling
     * @see khome.scheduling
     *
     */
    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    fun connect(reactOnStateChangeEvents: suspend DefaultClientWebSocketSession.() -> Unit) {
        runBlocking {
            while (!connected) {
                delay(2000)
                try {
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
                } catch (e: Throwable) {
                    logger.error { e.message }
                    reconnect()
                }
            }
        }
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

    reactOnStateChangeEvents()

    if (config.runIntegrityTests)
        runIntegrityTest()

    if (successfullyStartedStateStream())
        consumeStateChangesByTriggeringEvents()
    else
        throw EventStreamException("Could not subscribe to event stream!")
}

private fun WebSocketSession.runIntegrityTest() = runInSandBoxMode {

    logger.info { "Testing the application:" }
    println("###      Integrity testing started     ###")

    val failCount = AtomicInteger(0)
    val now = LocalDateTime.now()
    val events = states.map { (entityId, state) ->
        async {
            val data = EventResult.Event.Data(entityId, state, state)
            val event = EventResult.Event("integrity_test_event", data, now.toDate(), "local")
            val eventResult = EventResult(idCounter.get(), "integrity_test", event)

            val success = catchAllTests(entityId) {
                stateChangeEvents(eventResult)
            }
            if (!success) failCount.incrementAndGet()

            entityId
        }
    }

    runBlocking {
        events.awaitAll().forEach { entityId ->
            stateChangeEvents.minus(entityId)
        }
    }

    val timeBasedActions = schedulerTestEvents.listeners.toTypedArray()

    timeBasedActions.forEach { action ->
        val success = catchAllTests("Timer") {
            action.value.invoke("Integrity_test")
        }
        if (!success) failCount.incrementAndGet()
    }

    val failCountTotal = failCount.get()
    when {
        failCountTotal == 0 -> {
            println(
                """
            +++ All tests passed the specifications +++
            ###      Integrity testing finished     ###
            """.trimIndent()
            )
            logger.info { "Application started" }
        }
        failCountTotal > 0 -> {
            println(
                """

                --- $failCountTotal test(s) did not pass the specifications ---
                ###      Integrity testing finished     ###
            """.trimIndent()

            )
            logger.error { "--- $failCountTotal test(s) fails ---" }
            logger.info { "Shutdown application" }
            logger.info { "Good bye" }
            exitProcess(1)
        }
    }
}

private inline fun catchAllTests(section: String, action: () -> Unit): Boolean {
    try {
        action()
        return true
    } catch (t: Throwable) {
        println(
            """

                ---  [$section]  ---
                Failed with message: ${t.message}
                ${t.stackTrace[0]}
                ---  [$section]  ---

            """.trimIndent()

        )
        return false
    }
}

@ObsoleteCoroutinesApi
suspend fun WebSocketSession.consumeStateChangesByTriggeringEvents() {
    coroutineScope {
        incoming.consumeEach { frame ->
            try {
                val message = frame.asObject<Map<String, Any>>()
                val type = message["type"]

                when (type) {
                    "event" -> launch {
                        updateLocalStateStore(frame)
                        emitStateChangeEvent(frame)
                    }
                    "result" -> launch { resolveResultTypeAndEmitEvents(frame) }
                    else -> launch { logger.warn { "Could not classify message: $type" } }
                }
            } catch (e: Throwable) {
                logger.info { e.message }
                close(e)
                reconnect()
            }
        }
    }
}

private fun resolveResultTypeAndEmitEvents(frame: Frame) {
    val resultData = frame.asObject<Result>()

    when {
        !resultData.success -> emitResultErrorEventAndPrintLogMessage(resultData)
        resultData.success && resultData.result is ArrayList<*> -> checkLocalStateStoreAndRefresh(frame)
        resultData.success && resultData.result is Map<*, *> -> {
            emitResultEvent(frame)
            logResults(resultData)
        }
    }
}

fun checkLocalStateStoreAndRefresh(frame: Frame) {
    val states = frame.asObject<StateResult>()
    var noneEqualStateCount = 0

    logger.debug { " ###    Started local state store check     ###" }

    states.result.forEach { state ->
        if (Khome.states[state.entityId] != state) {
            noneEqualStateCount++
            logger.warn { "The state of ${state.entityId} is not in sync any more." }
            Khome.states[state.entityId] = state
            logger.warn { "The state has been refreshed." }
        }
    }

    if (noneEqualStateCount > 0) logger.debug { """--- $noneEqualStateCount none equal states discovered --- """ }
    logger.debug { " ###    Local state store check finished    ###" }

}

private fun logResults(resultData: Result) =
    logger.info { "Result-Id: ${resultData.id} | Success: ${resultData.success}" }


private fun emitResultErrorEventAndPrintLogMessage(resultData: Result) {
    val errorCode = resultData.error?.get("code")!!
    val errorMessage = resultData.error.get("message")!!

    errorResultEvents(ErrorResult(errorCode, errorMessage))
    logger.error { "$errorCode: $errorMessage" }
}

private fun emitStateChangeEvent(frame: Frame) = stateChangeEvents(frame.asObject())

private fun emitResultEvent(frame: Frame) = resultEvents(frame.asObject())

suspend fun WebSocketSession.updateLocalStateStore(frame: Frame) {
    val data = frame.asObject<EventResult>()
    if (states[data.event.data.entityId] == data.event.data.newState) fetchStates()
    else states[data.event.data.entityId] = data.event.data.newState
}

suspend fun WebSocketSession.fetchAvailableServicesFromApi() {
    val payload = FetchServices(idCounter.incrementAndGet())
    callWebSocketApi(payload.toJson())
    val message = getMessage<ServiceResult>()

    message.result.forEach { (domain, services) ->
        val serviceCollection = mutableListOf<String>()
        services.forEach { (name, _) ->
            serviceCollection.add(name)
        }
        Khome.services[domain] = serviceCollection
    }
}

suspend fun WebSocketSession.startStateStream() {
    fetchStates()
    val message = getMessage<StateResult>()

    message.result.forEach { state ->
        states[state.entityId] = state
    }
    callWebSocketApi(ListenEvent(idCounter.incrementAndGet(), eventType = "state_changed").toJson())
}

suspend fun WebSocketSession.fetchStates() = callWebSocketApi(FetchStates(idCounter.incrementAndGet()).toJson())

suspend fun WebSocketSession.callWebSocketApi(content: String) = send(content)

suspend fun WebSocketSession.successfullyStartedStateStream() = getMessage<Result>().success

suspend inline fun <reified M : Any> WebSocketSession.getMessage(): M = incoming.receive().asObject()

inline fun <reified M : Any> Frame.asObject() = (this as Frame.Text).toObject<M>()

data class FetchStates(val id: Int, override val type: String = "get_states") : MessageInterface
data class FetchServices(val id: Int, override val type: String = "get_services") : MessageInterface
data class Ping(val id: Int, override val type: String = "ping") : MessageInterface
data class ErrorResult(val code: String, val message: String)