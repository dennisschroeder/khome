package khome.testing

import com.google.gson.JsonObject
import io.ktor.util.KtorExperimentalAPI
import khome.ActuatorsByApiName
import khome.ActuatorsByEntity
import khome.HassAPiCommandHistory
import khome.SensorsByApiName
import khome.communicating.HassApiClient
import khome.core.boot.statehandling.flattenStateAttributes
import khome.core.koin.KhomeKoinContext
import khome.core.mapping.ObjectMapperInterface
import khome.core.mapping.fromJson
import khome.entities.ActuatorStateUpdater
import khome.entities.SensorStateUpdater
import khome.entities.devices.Actuator
import khome.values.EntityId
import kotlinx.coroutines.ObsoleteCoroutinesApi
import mu.KotlinLogging
import org.koin.dsl.module

@ExperimentalStdlibApi
internal class KhomeTestApplicationImpl(
    private val sensorsByApiName: SensorsByApiName,
    private val actuatorsByApiName: ActuatorsByApiName,
    private val actuatorsByEntity: ActuatorsByEntity,
    private val mapper: ObjectMapperInterface,
    private val hassAPiCommandHistory: HassAPiCommandHistory
) : KhomeTestApplication {

    private val logger = KotlinLogging.logger { }

    init {
        val testClient = module(override = true) {
            single<HassApiClient> { HassApiTestClient(get()) }
        }

        KhomeKoinContext.addModule(testClient)
    }

    @KtorExperimentalAPI
    @ObsoleteCoroutinesApi
    @ExperimentalStdlibApi
    override fun setStateAndAttributes(json: String) {
        val stateJson = mapper.fromJson<JsonObject>(json)
        val entityIdFromState = checkNotNull(stateJson["entity_id"])
        val entityId = EntityId.from(entityIdFromState.asString)

        actuatorsByApiName[entityId]?.also {
            ActuatorStateUpdater(actuatorsByApiName).invoke(flattenStateAttributes(stateJson), entityId)
            logger.info { "Set actuator state for $entityId" }
        }

        sensorsByApiName[entityId]?.also {
            SensorStateUpdater(sensorsByApiName).invoke(flattenStateAttributes(stateJson), entityId)
            logger.info { "Set sensor state for $entityId" }
        }
    }

    override fun lastApiCommandFrom(entity: Actuator<*, *>): String =
        actuatorsByEntity[entity]?.let { entityId ->
            hassAPiCommandHistory[entityId]?.let { command ->
                mapper.toJson(command)
            } ?: throw IllegalStateException("No command found for actuator with id: $entityId")
        } ?: throw IllegalStateException("No actuator found.")

    fun reset() {
        hassAPiCommandHistory.clear()
    }
}
