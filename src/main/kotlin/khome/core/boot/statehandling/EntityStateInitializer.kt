package khome.core.boot.statehandling

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.core.boot.BootSequenceInterface
import khome.core.koin.CallerID
import khome.core.koin.KhomeKoinComponent
import khome.entities.ActuatorStateUpdater
import khome.entities.SensorStateUpdater
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging

@OptIn(ObsoleteCoroutinesApi::class, KtorExperimentalAPI::class)
internal class EntityStateInitializer(
    override val khomeSession: KhomeSession,
    private val callerID: CallerID,
    private val sensorStateUpdater: SensorStateUpdater,
    private val actuatorStateUpdater: ActuatorStateUpdater
) : BootSequenceInterface, KhomeKoinComponent {

    private val logger = KotlinLogging.logger { }
    private val id
        get() = callerID.incrementAndGet()

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
                    sensorStateUpdater(initialState)
                    actuatorStateUpdater(initialState)
                }
                logger.info { "All initial entity states are set." }
            }
        }
}
