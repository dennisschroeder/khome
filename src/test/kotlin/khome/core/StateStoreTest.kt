package khome.core

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.ktor.util.KtorExperimentalAPI
import khome.core.dependencyInjection.KhomeTestComponent
import khome.core.mapping.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.get

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class StateStoreTest : KhomeTestComponent() {

    private val stateJson = """
            {
                "entity_id":"light.bed_light",
                "last_changed":"2016-11-26T01:37:24.265390+00:00",
                "state":"on",
                "attributes":{
                   "rgb_color":[
                      254,
                      208,
                      0
                   ],
                   "color_temp":380,
                   "supported_features":147,
                   "xy_color":[
                      0.5,
                      0.5
                   ],
                   "brightness":180,
                   "white_value":200,
                   "friendly_name":"Bed Light"
                }
            }
        """.trimIndent()
    private val newStateJson = """
            {
                "entity_id":"light.bed_light",
                "last_changed":"2016-11-26T01:39:45.265390+00:00",
                "state":"off",
                "attributes":{
                   "rgb_color":[
                      254,
                      208,
                      0
                   ],
                   "color_temp":380,
                   "supported_features":147,
                   "xy_color":[
                      0.5,
                      0.5
                   ],
                   "brightness":180,
                   "white_value":200,
                   "friendly_name":"Bed Light"
                }
            }
        """.trimIndent()

    @Test
    fun `assert StateStore returns injected State by entity id`() {
        val state: State = get<ObjectMapper>().fromJson(stateJson)
        val stateStore = StateStore()
        stateStore["light.bed_light"] = state
        assertThat(stateStore["light.bed_light"]).isEqualTo(state)
    }

    @Test
    fun `assert StateStore updates State when overridden by entity id`() {
        val stateStore = StateStore()
        val state: State = get<ObjectMapper>().fromJson(stateJson)
        val newState: State = get<ObjectMapper>().fromJson(newStateJson)
        stateStore["light.bed_light"] = state
        stateStore["light.bed_light"] = newState

        assertThat(stateStore["light.bed_light"]).isEqualTo(newState)
    }

    @Test
    fun `assert read from different coroutine`() {
        val state: State = get<ObjectMapper>().fromJson(stateJson)
        val newState: State = get<ObjectMapper>().fromJson(newStateJson)
        runBlocking {
            val stateStore = StateStore()
            logger.info { "State: $newState" }
            stateStore["light.bed_light"] = state

            logger.info { "NewState: $newState" }
            stateStore["light.bed_light"] = newState

            launch(Dispatchers.Default) {
                logger.info { "Accessing newState from another coroutine" }
                assertThat(stateStore["light.bed_light"]).isEqualTo(newState)
            }
        }
    }

    @Test
    fun `returns null when state not found`() {
        val stateStore = StateStore()
        val state: State = get<ObjectMapper>().fromJson(stateJson)
        stateStore["light.bed_light"] = state

        assertThat(stateStore["light.bathroom_light"]).isNull()
    }

    @Test
    fun `assert call to clear removes all items`() {
        val stateStore = StateStore()
        val state: State = get<ObjectMapper>().fromJson(stateJson)
        stateStore["light.bed_light"] = state

        stateStore.clear()

        assertThat(stateStore).isEmpty()
    }
}
