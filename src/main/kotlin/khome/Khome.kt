package khome

import io.ktor.http.cio.websocket.Frame
import io.ktor.util.KtorExperimentalAPI
import khome.calling.FetchServices
import khome.calling.FetchStates
import khome.core.ConfigurationInterface
import khome.core.CustomEventResult
import khome.core.EventResult
import khome.core.ListenEvent
import khome.core.Result
import khome.core.ServiceResult
import khome.core.ServiceStoreInterface
import khome.core.StateResult
import khome.core.StateStoreInterface
import khome.core.authenticate
import khome.core.dependencyInjection.CallerID
import khome.core.dependencyInjection.KhomeComponent
import khome.core.dependencyInjection.KhomeKoinContext
import khome.core.dependencyInjection.KhomeModule
import khome.core.dependencyInjection.khomeModule
import khome.core.dependencyInjection.loadKhomeModule
import khome.core.entities.system.DateTime
import khome.core.entities.system.Sun
import khome.core.entities.system.Time
import khome.core.eventHandling.CustomEvent
import khome.core.eventHandling.CustomEventRegistry
import khome.core.eventHandling.FailureResponseEvent
import khome.core.eventHandling.StateChangeEvent
import khome.core.exceptions.EventStreamException
import khome.core.logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.logger.Level
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
class Khome : KhomeComponent() {
    companion object {
        private var sandboxMode = AtomicBoolean(false)

        val isSandBoxModeActive get() = sandboxMode.get()
        private fun sandBoxModeOn() = sandboxMode.set(true)
        private fun sandBoxModeOff() = sandboxMode.set(false)

        var beanDeclarations: KhomeModule.() -> Unit = {}
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

    fun beans(beanDeclarations: KhomeModule.() -> Unit) {
        Khome.beanDeclarations = beanDeclarations
    }

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
    suspend fun connectAndRun(listeners: suspend KhomeSession.() -> Unit) =
        coroutineScope {
            get<KhomeClient>()
                .startSession {
                    configureLogger(get())
                    runApplication(get(), listeners)
                }
        }
}

internal fun KhomeSession.configureLogger(config: ConfigurationInterface) {
    System.setProperty(
        org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY,
        if (config.logLevel == "DEBUG") "TRACE" else config.logLevel
    )
    System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_DATE_TIME_KEY, "${config.logTime}")
    System.setProperty(org.slf4j.impl.SimpleLogger.DATE_TIME_FORMAT_KEY, config.logTimeFormat)
    System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, config.logOutput)
    KhomeKoinContext.application?.let { it.printLogger(Level.valueOf(config.logLevel)) }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
private suspend fun KhomeSession.runApplication(
    config: ConfigurationInterface,
    listeners: suspend KhomeSession.() -> Unit
) {
    authenticate(get())
    fetchServices(get())
    storeServices(consumeMessage(), get())

    if (config.startStateStream) {
        fetchStates(get())
        storeStates(consumeMessage(), get())
        subscribeStateChanges(get())
    }

    val systemEntityBeans = khomeModule(createdAtStart = true, override = true) {
        bean { Sun() }
        bean { Time() }
        bean { DateTime() }
    }

    loadKhomeModule(systemEntityBeans)
    loadKhomeModule(khomeModule(createdAtStart = true, override = true, moduleDeclaration = Khome.beanDeclarations))
    subscribeCustomEvents(get(), get())
    listeners()

    if (successfullyStartedStateStream()) {
        consumeStateChangesByTriggeringEvents()
    } else
        throw EventStreamException("Could not subscribe to event stream!")
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
private suspend fun KhomeSession.consumeStateChangesByTriggeringEvents() = coroutineScope {
    val stateChangeEvent: StateChangeEvent by inject()

    incoming.consumeEach { frame ->
        val message = frame.asObject<Map<String, Any>>()
        when (message["type"]) {
            "event" -> {
                if (determineEventType(frame) == "state_changed") {
                    updateLocalStateStore(frame, get())
                    stateChangeEvent.emit(frame.asObject())
                } else {
                    getCustomEventOrNull(frame)?.let { event ->
                        frame.asObject<CustomEventResult>().event.data.let { eventData ->
                            event.emit(eventData)
                        }
                    }
                }
            }
            "result" -> {
                resolveResultTypeAndEmitEvents(frame)
            }
            else -> logger.warn { "Could not classify message: ${message["type"]}" }
        }
    }
}

private fun KhomeSession.getCustomEventOrNull(frame: Frame): CustomEvent? {
    val registry = get<CustomEventRegistry>()
    val eventType = determineEventType(frame)
    if (eventType in registry) return registry[eventType]

    logger.warn { "Custom event: \"${determineEventType(frame)}\" is not registered in this application." }
    return null
}

private fun KhomeSession.determineEventType(frame: Frame): String =
    frame.asObject<EventResult>().event.eventType

private suspend fun KhomeSession.resolveResultTypeAndEmitEvents(frame: Frame) {
    val resultData = frame.asObject<Result>()
    logger.debug { "Result: $resultData" }
    when {
        !resultData.success -> emitResultErrorEventAndPrintLogMessage(resultData, get())
        resultData.success && resultData.result is ArrayList<*> -> checkLocalStateStoreAndRefresh(frame)
        resultData.success -> {
            logResults(resultData)
        }
    }
}

private fun KhomeSession.checkLocalStateStoreAndRefresh(frame: Frame) {
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

private fun KhomeSession.logResults(resultData: Result) =
    logger.info { "Result-Id: ${resultData.id} | Success: ${resultData.success}" }

private fun KhomeSession.emitResultErrorEventAndPrintLogMessage(
    resultData: Result,
    failureResponseEvent: FailureResponseEvent
) {
    failureResponseEvent.emit(resultData)
    logger.error { "CallId: ${resultData.id} -  errorCode: ${resultData.error!!.code} ${resultData.error.message}" }
}

private fun KhomeSession.updateLocalStateStore(frame: Frame, stateStore: StateStoreInterface) {
    val data = frame.asObject<EventResult>()
    data.event.data.newState?.let { stateStore[data.event.data.entityId] = it }
}

internal suspend fun KhomeSession.fetchServices(id: CallerID) {
    val payload = FetchServices(id.incrementAndGet())
    callWebSocketApi(payload.toJson())
}

internal fun KhomeSession.storeServices(
    serviceResult: ServiceResult,
    serviceStore: ServiceStoreInterface
) =
    serviceResult
        .result
        .forEach { (domain, services) ->
            logger.debug { "$domain: $services" }
            val serviceList = mutableListOf<String>()
            services.forEach { (name, _) ->
                serviceList += name
                logger.debug { "Fetched service: $name from domain: $domain" }
            }
            serviceStore[domain] = serviceList
        }

internal suspend fun KhomeSession.subscribeCustomEvents(id: CallerID, registry: CustomEventRegistry) {
    registry.forEach { eventType ->
        val id = id.incrementAndGet()
        callWebSocketApi(ListenEvent(id = id, eventType = eventType.key).toJson())
        logger.info { "CallerId: $id - Subscribed to custom event: $eventType" }
    }
}

internal suspend fun KhomeSession.subscribeStateChanges(id: CallerID) =
    callWebSocketApi(ListenEvent(id.incrementAndGet(), eventType = "state_changed").toJson())

internal fun KhomeSession.storeStates(stateResults: StateResult, stateStore: StateStoreInterface) =
    stateResults.result
        .forEach { state ->
            stateStore[state.entityId] = state
            logger.debug { "Fetched state with data: ${stateStore[state.entityId]}" }
        }

internal suspend fun KhomeSession.fetchStates(id: CallerID) =
    callWebSocketApi(FetchStates(id.incrementAndGet()).toJson())

private suspend fun KhomeSession.successfullyStartedStateStream() = consumeMessage<Result>().success
