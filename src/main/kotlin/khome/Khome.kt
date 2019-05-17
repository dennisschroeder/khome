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
import khome.Khome.Companion.stateChangeEvents
import khome.core.exceptions.EventStreamException
import kotlinx.coroutines.channels.consumeEach

fun initialize(init: Khome.() -> Unit): Khome {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
    return Khome().apply(init)
}

class Khome {
    companion object {
        val states = mutableMapOf<String, State>()
        val stateChangeEvents = Event<EventResult>()
        val resultEvents = Event<Result>()
        val config = Configuration()

        /**
         * Since the Homeassistant web socket API needs an incrementing
         * id when calling it, we need to provide the callService feature
         * with such an id.
         *
         * @see "https://developers.home-assistant.io/docs/en/external_api_websocket.html#message-format"
         */
        private var idCounter: Int = 10000

        @Synchronized fun incrementIdCounter() = idCounter++
        @Synchronized fun incrementIdCounterAndFetchNextId(): Int {
            incrementIdCounter()
            return idCounter
        }
    }

    private val method = HttpMethod.Get
    private val path = "/api/websocket"

    fun configure(init: Configuration.() -> Unit) {
        config.apply(init)
    }

    @KtorExperimentalAPI
    private val client = HttpClient(CIO).config {
        install(WebSockets)
    }

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    suspend fun connect(reactOnStateChangeEvents: suspend DefaultClientWebSocketSession.() -> Unit) {
        client.ws(
            method = method,
            host = config.host,
            port = config.port,
            path = path
        ) {
            val run = runCatching {
                authenticate(config.accessToken)
                if (config.startStateStream) {
                    startStateStream()
                }
                if (successfullyStartedStateStream()) {
                    logResults()
                    reactOnStateChangeEvents()
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

    suspend inline fun <reified S> hasStateChangedAfterTime(entityId: String, time: Long): Boolean {
        val presentState = states[entityId]?.getValue<S>()
        delay(time)
        val futureState = states[entityId]?.getValue<S>()
        return presentState == futureState
    }

    suspend inline fun <reified A> hasAttributeChangedAfterTime(
        entityId: String,
        time: Long,
        attribute: String
    ): Boolean {
        val presentAttr = states[entityId]?.getAttribute<A>(attribute)
        delay(time)
        val futureAttr = states[entityId]?.getAttribute<A>(attribute)
        return presentAttr == futureAttr
    }
}

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

data class Configuration(
    var host: String = "localhost",
    var port: Int = 8123,
    var accessToken: String = "<create one in home-assistant>",
    var startStateStream: Boolean = true
)

suspend fun WebSocketSession.startStateStream() {
    callWebSocketApi(FetchStates(1000).toJson())
    val message = getMessage<StateResult>()

    message.result.forEach {
        states[it.entityId] = it
    }
    callWebSocketApi(ListenEvent(1100, eventType = "state_changed").toJson())
}

suspend fun WebSocketSession.callWebSocketApi(content: String) = send(content)

suspend fun WebSocketSession.successfullyStartedStateStream() = getMessage<Result>().success

fun logResults() {
    resultEvents += {
        logger.info { "Result-Id: ${it.id} | Success: ${it.success}" }
        if (null != it.error) logger.error { "${it.error["code"]}: ${it.error["message"]}" }
    }
}

suspend inline fun <reified M : Any> WebSocketSession.getMessage(): M = incoming.receive().asObject()

inline fun <reified M : Any> Frame.asObject() = (this as Frame.Text).toObject<M>()

data class ListenEvent(
    val id: Int,
    override val type: String = "subscribe_events",
    val eventType: String
) : MessageInterface

data class FetchStates(val id: Int, override val type: String = "get_states") : MessageInterface
