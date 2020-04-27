package khome.core

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isSuccess
import io.ktor.util.KtorExperimentalAPI
import khome.core.dependencyInjection.KhomeTestComponent
import khome.core.servicestore.ServiceStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ServiceStoreTest : KhomeTestComponent() {

    private val serviceList = listOf("turn_on", "turn_off", "toggle", "stop", "start")

    @Test
    fun `assert ServiceStore returns injected service by domain`() {
        val serviceStore = ServiceStore()
        serviceStore["homeassistant"] = serviceList

        assertThat(serviceStore["homeassistant"]).isEqualTo(serviceList)
    }

    @Test
    fun `assert read from different coroutine`() {
        val serviceStore = ServiceStore()
        serviceStore["homeassistant"] = serviceList

        runBlocking {
            launch(Dispatchers.Default) {
                assertThat { serviceStore["homeassistant"] }.isSuccess()
            }
        }
    }

    @Test
    fun `returns null when service not found`() {
        val serviceStore = ServiceStore()
        serviceStore["homeassistant"] = serviceList

        assertThat(serviceStore["wrong_domain"]).isNull()
    }

    @Test
    fun `assert call to clear removes all items`() {
        val serviceStore = ServiceStore()
        serviceStore["homeassistant"] = serviceList

        serviceStore.clear()

        assertThat(serviceStore).isEmpty()
    }
}
