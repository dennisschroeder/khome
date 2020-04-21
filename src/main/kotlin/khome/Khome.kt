package khome

import io.ktor.http.cio.websocket.Frame
import io.ktor.util.KtorExperimentalAPI
import khome.core.BaseKhomeComponent
import khome.core.ConfigurationInterface
import khome.core.EventResult
import khome.core.FetchServices
import khome.core.FetchStates
import khome.core.HassEventResultDto
import khome.core.ListenEvent
import khome.core.NewState
import khome.core.OldState
import khome.core.Result
import khome.core.ServiceResult
import khome.core.ServiceStoreInterface
import khome.core.State
import khome.core.StateResult
import khome.core.StateStoreEntry
import khome.core.StateStoreInterface
import khome.core.authenticate
import khome.core.dependencyInjection.CallerID
import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.dependencyInjection.KhomeKoinContext
import khome.core.dependencyInjection.KhomeModule
import khome.core.dependencyInjection.khomeModule
import khome.core.dependencyInjection.loadKhomeModule
import khome.core.entities.system.DateTime
import khome.core.entities.system.Sun
import khome.core.entities.system.Time
import khome.core.eventHandling.FailureResponseEvent
import khome.core.eventHandling.HassEvent
import khome.core.eventHandling.HassEventRegistry
import khome.core.eventHandling.StateChangeEvent
import khome.core.exceptions.EventStreamException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import org.koin.core.get
import org.koin.core.inject

/**
 * The main entry point to start your application
 *
 * @param init The type safe builder function to access the receiver
 * @return instance of Khome class instantiated with default values.
 */

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun khomeApplication(init: Khome.() -> Unit): KhomeApplication {
    KhomeKoinContext.startKoinApplication()
    val khome = Khome().apply(init)
    return khome.get()
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
        var beanDeclarations: KhomeModule.() -> Unit = {}
    }

    /**
     * Configure your Khome instance. See all available properties in
     * the [ConfigurationInterface] data class.
     *
     * @param builder Lambda with receiver to configure Khome
     * @see [ConfigurationInterface]
     */
    fun configure(builder: ConfigurationInterface.() -> Unit) {
        val config: ConfigurationInterface by inject()
        config.apply(builder)
    }

    fun beans(beanDeclarations: KhomeModule.() -> Unit) {
        Khome.beanDeclarations = beanDeclarations
    }
}

@KtorExperimentalAPI
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
internal suspend fun KhomeSession.initiateApplication(
    config: ConfigurationInterface,
    listeners: suspend BaseKhomeComponent.() -> Unit
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
    loadKhomeModule(khomeModule(createdAtStart = true, moduleDeclaration = Khome.beanDeclarations))
    subscribeHassEvents(get(), get())
    listeners(BaseKhomeComponent())

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
                    getHassEventOrNull(frame)?.let { event ->
                        frame.asObject<HassEventResultDto>().event.data.let { eventData ->
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

private fun KhomeSession.getHassEventOrNull(frame: Frame): HassEvent? {
    val registry = get<HassEventRegistry>()
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
        resultData.success -> { logResults(resultData) }
    }
}

private fun KhomeSession.logResults(resultData: Result) =
    logger.info { "Result-Id: ${resultData.id} | Success: ${resultData.success}" }

private suspend fun KhomeSession.emitResultErrorEventAndPrintLogMessage(
    resultData: Result,
    failureResponseEvent: FailureResponseEvent
) {
    failureResponseEvent.emit(resultData)
    logger.error { "CallId: ${resultData.id} -  errorCode: ${resultData.error!!.code} ${resultData.error.message}" }
}

private fun KhomeSession.updateLocalStateStore(frame: Frame, stateStore: StateStoreInterface) {
    val data = frame.asObject<EventResult>()
    if (data.event.data.oldState == null && data.event.data.newState == null) throw IllegalStateException("Both states (old and new) are null in ${data.event.data.entityId}")
    val stateStoreEntryById = stateStore[data.event.data.entityId]

    if (stateStoreEntryById !== null) {
        data.event.data.oldState?.let { state ->
            val updatedState = stateStoreEntryById.copy(oldState = OldState(state))
            stateStore[data.event.data.entityId] = updatedState
        }

        data.event.data.newState?.let { state ->
            val updatedState = stateStoreEntryById.copy(newState = NewState(state))
            stateStore[data.event.data.entityId] = updatedState
        }
    } else {
        logger.info { "New entity detected: ${data.event.data.entityId}. Restart your application to have it available." }
    }
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

internal suspend fun KhomeSession.subscribeHassEvents(id: CallerID, registry: HassEventRegistry) {
    registry.forEach { eventType ->
        val id = id.incrementAndGet()
        callWebSocketApi(ListenEvent(id = id, eventType = eventType.key).toJson())
        logger.info { "CallerId: $id - Subscribed to custom event: ${eventType.key}" }
    }
}

internal suspend fun KhomeSession.subscribeStateChanges(id: CallerID) =
    callWebSocketApi(ListenEvent(id.incrementAndGet(), eventType = "state_changed").toJson())

internal fun KhomeSession.storeStates(stateResults: StateResult, stateStore: StateStoreInterface) =
    stateResults.result
        .forEach { state ->
            createEntryInStateStore(stateStore, state, state)
            logger.debug { "Fetched state with data: ${stateStore[state.entityId]}" }
        }

internal fun KhomeSession.createEntryInStateStore(stateStore: StateStoreInterface, oldState: State, newState: State) {
    stateStore[newState.entityId] = StateStoreEntry(OldState(oldState), NewState(newState))
}

internal suspend fun KhomeSession.fetchStates(id: CallerID) =
    callWebSocketApi(FetchStates(id.incrementAndGet()).toJson())

private suspend fun KhomeSession.successfullyStartedStateStream() = consumeMessage<Result>().success
