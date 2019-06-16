package khome

import khome.core.*
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import java.time.LocalDateTime
import io.ktor.http.HttpMethod
import khome.scheduling.toDate
import kotlin.system.exitProcess
import io.ktor.client.HttpClient
import io.ktor.http.cio.websocket.*
import khome.Khome.Companion.states
import io.ktor.client.engine.cio.CIO
import khome.Khome.Companion.idCounter
import io.ktor.util.KtorExperimentalAPI
import khome.Khome.Companion.resultEvents
import io.ktor.client.features.websocket.*
import khome.Khome.Companion.timeBasedEvents
import khome.Khome.Companion.errorResultEvents
import khome.Khome.Companion.stateChangeEvents
import kotlinx.coroutines.channels.consumeEach
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import khome.Khome.Companion.activateSandBoxMode
import khome.core.exceptions.EventStreamException
import khome.Khome.Companion.deactivateSandBoxMode
import khome.Khome.Companion.runInSandBoxMode
import kotlinx.coroutines.channels.ClosedReceiveChannelException

fun initialize(init: Khome.() -> Unit): Khome {
    return Khome().apply(init)
}

class Khome {
    companion object {
        val states = hashMapOf<String, State>()
        val services = hashMapOf<String, List<String>>()
        val stateChangeEvents = Event<EventResult>()
        val timeBasedEvents = Event<String>()
        val resultEvents = Event<Result>()
        val errorResultEvents = Event<ErrorResult>()
        val config = Configuration()

        /**
         * Since the Homeassistant web socket API needs an incrementing
         * id when calling it, we need to provide the callService feature
         * with such an id.
         *
         * @see "https://developers.home-assistant.io/docs/en/external_api_websocket.html#message-format"
         */
        var idCounter = AtomicInteger(0)

        private var sandboxMode = AtomicBoolean(false)

        fun isSandBoxModeActive() = sandboxMode.get()
        fun activateSandBoxMode() = sandboxMode.set(true)
        fun deactivateSandBoxMode() = sandboxMode.set(false)
        fun runInSandBoxMode(action: () -> Unit) {
            activateSandBoxMode()
            action()
            deactivateSandBoxMode()
        }

        @ObsoleteCoroutinesApi
        val callServiceContext = newSingleThreadContext("ServiceContext")
    }

    private val method = HttpMethod.Get
    private val path = "/api/websocket"

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

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    fun connect(reactOnStateChangeEvents: suspend DefaultClientWebSocketSession.() -> Unit) {
        runBlocking {
            if (config.secure) client.wss(
                method = method,
                host = config.host,
                port = config.port,
                path = path
            ) { runApplication(config, reactOnStateChangeEvents) }
            else client.ws(
                method = method,
                host = config.host,
                port = config.port,
                path = path
            ) { runApplication(config, reactOnStateChangeEvents) }
        }
    }
}

@ObsoleteCoroutinesApi
private suspend fun DefaultClientWebSocketSession.runApplication(
    config: Configuration,
    reactOnStateChangeEvents: suspend DefaultClientWebSocketSession.() -> Unit
) =
    try {
        authenticate(config.accessToken)
        fetchAvailableServicesFromApi()
        if (config.startStateStream) startStateStream()
        reactOnStateChangeEvents()
        if (config.runIntegrityTests) runIntegrityTest()
        if (successfullyStartedStateStream()) consumeStateChangesByTriggeringEvents()
        else throw EventStreamException("Could not subscribe to event stream!")
    } catch (e: ClosedReceiveChannelException) {
        logger.error(e) { "Connection was closed!" }
    } catch (e: Throwable) {
        logger.error(e) { e.stackTrace }
    }


// ToDo("Refactor into several functions")
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

    val timeBasedActions = timeBasedEvents.listeners.toTypedArray()

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

inline fun <reified S> hasStateChangedAfterTime(entityId: String, time: Long): Boolean {
    val presentState = states[entityId]?.getValue<S>()
    sleep(time)
    val futureState = states[entityId]?.getValue<S>()
    return presentState == futureState
}

inline fun <reified A> hasAttributeChangedAfterTime(
    entityId: String,
    time: Long,
    attribute: String
): Boolean {
    val presentAttr = states[entityId]?.getAttribute<A>(attribute)
    sleep(time)
    val futureAttr = states[entityId]?.getAttribute<A>(attribute)
    return presentAttr == futureAttr
}

@ObsoleteCoroutinesApi
suspend fun WebSocketSession.consumeStateChangesByTriggeringEvents() {
    coroutineScope {
        incoming.consumeEach { frame ->
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

data class ErrorResult(val code: String, val message: String)