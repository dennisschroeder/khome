package khome

import khome.core.*
import kotlinx.coroutines.*
import io.ktor.http.HttpMethod
import io.ktor.client.HttpClient
import io.ktor.http.cio.websocket.*
import khome.Khome.Companion.states
import io.ktor.client.engine.cio.CIO
import io.ktor.util.KtorExperimentalAPI
import khome.Khome.Companion.resultEvents
import io.ktor.client.features.websocket.*
import khome.Khome.Companion.activateSandBoxMode
import khome.Khome.Companion.deactivateSandBoxMode
import khome.Khome.Companion.errorResultEvents
import khome.Khome.Companion.idCounter
import khome.Khome.Companion.stateChangeEvents
import khome.Khome.Companion.timeBasedEvents
import khome.core.exceptions.EventStreamException
import khome.scheduling.toDate
import kotlinx.coroutines.channels.consumeEach
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

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
            client.ws(
                method = method,
                host = config.host,
                port = config.port,
                path = path
            ) {
                val run = runCatching {
                    authenticate(config.accessToken)
                    fetchAvailableServicesFromApi()
                    if (config.startStateStream) {
                        startStateStream()
                    }
                    if (successfullyStartedStateStream()) {
                        logResults()
                        emitEventOnResultError()
                        reactOnStateChangeEvents()
                        if (config.runIntegrityTests) runIntegrityTest()
                        consumeStateChangesByTriggeringEvents()
                    } else {
                        throw EventStreamException("Could not subscribe to event stream!")
                    }
                }
                run.onFailure {
                    logger.error(it) { it.printStackTrace() }
                }
            }
        }
    }
}

// ToDo("Refactor into several functions")
fun WebSocketSession.runIntegrityTest() {
    activateSandBoxMode()

    logger.info { "Testing the application:" }
    println("###      Integrity testing started     ###")

    val fails = AtomicInteger(0)
    val now = LocalDateTime.now()
    val events = states.map { (entityId, state) ->
        async {
            val data = EventResult.Event.Data(entityId, state, state)
            val event = EventResult.Event("integrity_test_event", data, now.toDate(), "local")
            val eventResult = EventResult(idCounter.get(), "integrity_test", event)

            val success = catchAllTests(entityId) {
                stateChangeEvents(eventResult)
            }
            if (!success) fails.incrementAndGet()

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
        if (!success) fails.incrementAndGet()
    }

    val failCount = fails.get()
    when {
        failCount == 0 -> {
            println(
                """
            +++ All tests passed the specifications +++
            ###      Integrity testing finished     ###
            """.trimIndent()
            )
            logger.info { "Application started" }
        }
        failCount > 0 -> {
            println(
                """

                --- $failCount test(s) did not pass the specifications ---
                ###      Integrity testing finished     ###
            """.trimIndent()

            )
            logger.error { "--- $failCount test(s) fails ---" }
            logger.info { "Shutdown application" }
            logger.info { "Good bye" }
            exitProcess(1)
        }
    }

    deactivateSandBoxMode()
}

inline fun catchAllTests(section: String, action: () -> Unit): Boolean {
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
                    triggerStateChangeEvent(frame)
                }
                "result" -> launch { triggerResultEvent(frame) }
                else -> launch { logger.warn { "Could not classify message: $type" } }
            }
        }
    }
}

fun triggerStateChangeEvent(frame: Frame) = stateChangeEvents(frame.asObject())

fun triggerResultEvent(frame: Frame) = resultEvents(frame.asObject())

fun updateLocalStateStore(frame: Frame) {
    val data = frame.asObject<EventResult>()
    states[data.event.data.entityId] = data.event.data.newState
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
    callWebSocketApi(FetchStates(idCounter.incrementAndGet()).toJson())
    val message = getMessage<StateResult>()

    message.result.forEach {
        states[it.entityId] = it
    }
    callWebSocketApi(ListenEvent(idCounter.incrementAndGet(), eventType = "state_changed").toJson())
}

suspend fun WebSocketSession.callWebSocketApi(content: String) = send(content)

suspend fun WebSocketSession.successfullyStartedStateStream() = getMessage<Result>().success

fun logResults() {
    resultEvents += {
        logger.info { "Result-Id: ${it.id} | Success: ${it.success}" }
        if (it.error != null) logger.error { "${it.error["code"]}: ${it.error["message"]}" }
    }
}

fun emitEventOnResultError() {
    resultEvents += {
        if (it.error != null) errorResultEvents(ErrorResult(it.error["code"]!!, it.error["message"]!!))
    }
}

suspend inline fun <reified M : Any> WebSocketSession.getMessage(): M = incoming.receive().asObject()

inline fun <reified M : Any> Frame.asObject() = (this as Frame.Text).toObject<M>()

data class FetchStates(val id: Int, override val type: String = "get_states") : MessageInterface
data class FetchServices(val id: Int, override val type: String = "get_services") : MessageInterface

data class ErrorResult(val code: String, val message: String)