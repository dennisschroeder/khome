package khome.core.boot.statehandling

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.core.boot.BootSequenceInterface
import khome.core.koin.KhomeComponent
import khome.entities.ActuatorStateUpdater
import khome.entities.SensorStateUpdater
import khome.communicating.CALLER_ID
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging

@OptIn(ObsoleteCoroutinesApi::class, KtorExperimentalAPI::class)
internal class EntityStateInitializer(
    override val khomeSession: KhomeSession,
    private val sensorStateUpdater: SensorStateUpdater,
    private val actuatorStateUpdater: ActuatorStateUpdater
) : BootSequenceInterface, KhomeComponent {

    private val logger = KotlinLogging.logger { }
    private val id
        get() = CALLER_ID.incrementAndGet()

    private val statesRequest = StatesRequest(id)

    @ExperimentalStdlibApi
    override suspend fun runBootSequence() {
        sendStatesRequest()
        logger.info { "Requested initial entity states" }
        setInitialEntityState(consumeStatesResponse())
    }

    private suspend fun sendStatesRequest() =
        khomeSession.callWebSocketApi(statesRequest)

    private suspend fun consumeStatesResponse() =
        khomeSession.consumeSingleMessage<StatesResponse>()

    @ExperimentalStdlibApi
    private fun setInitialEntityState(statesResponse: StatesResponse) =
        when (statesResponse.success) {
            false -> logger.error { "Could not fetch initial states from homeassistant" }
            true -> {
                statesResponse.result.forEach { initialState ->
                    logger.debug { "Initial state for: ${initialState.entityId} is $initialState" }
                    sensorStateUpdater(initialState)
                    actuatorStateUpdater(initialState)
                }
                logger.info { "All initial entity states are set." }
            }
        }
}
