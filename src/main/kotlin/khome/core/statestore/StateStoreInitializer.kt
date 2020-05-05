package khome.core.statestore

import khome.KhomeSession
import khome.core.boot.BootSequenceInterface
import khome.core.dependencyInjection.CallerID
import mu.KotlinLogging

internal class StateStoreInitializer(
    override val khomeSession: KhomeSession,
    private val callerID: CallerID,
    private val stateStore: StateStoreInterface
) : BootSequenceInterface {

    private val logger = KotlinLogging.logger { }
    private val id
        get() = callerID.incrementAndGet()

    private val statesRequest = StatesRequest(id)

    override suspend fun runBootSequence() {
        sendStatesRequest()
        logger.info { "Requested registered homeassistant entity states" }
        storeStates(consumeStatesResponse())
    }

    private suspend fun sendStatesRequest() =
        khomeSession.callWebSocketApi(statesRequest)

    private suspend fun consumeStatesResponse() =
        khomeSession.consumeSingleMessage<StatesResponse>()

    private fun storeStates(statesResponse: StatesResponse) =
        when (statesResponse.success) {
            false -> logger.error { "Could not fetch states from homeassistant" }
            true -> {
                statesResponse.result.forEach { state ->
                    stateStore[state.entityId] =
                        StateStoreEntry(state, state)
                    logger.debug { "Fetched state with data: ${stateStore[state.entityId]}" }
                }
                logger.info { "Stored homeassistant states in local state store" }
            }
        }
}
