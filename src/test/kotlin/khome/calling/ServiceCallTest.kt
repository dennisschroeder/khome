package khome.calling

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import assertk.assertions.matchesPredicate
import khome.calling.errors.DomainNotFoundException
import khome.calling.errors.ServiceNotFoundException
import khome.core.servicestore.ServiceStoreInterface
import khome.core.dependencyInjection.KhomeTestComponent
import khome.core.dependencyInjection.khomeModule
import khome.core.dependencyInjection.loadKhomeModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.core.error.InstanceCreationException
import org.koin.core.get

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ServiceCallTest : KhomeTestComponent() {

    @Test
    fun `happy path - create service call through dependency injection`() {
        val serviceStore: ServiceStoreInterface = get()
        serviceStore["homeassistant"] = listOf("update_entity")

        class CustomService :
            ServiceCall(Domain.HOMEASSISTANT, HomeAssistantService.UPDATE_ENTITY) {
            val serviceData = null
        }

        assertThat {
            val bean = khomeModule(createdAtStart = true) { bean { CustomService() } }
            loadKhomeModule(bean)
        }.isSuccess()

        val customService: CustomService = get()
        assertThat(customService.domain).isEqualTo(Domain.HOMEASSISTANT)
        assertThat(customService.service).isEqualTo(HomeAssistantService.UPDATE_ENTITY)
    }

    @Test
    fun `assert throws DomainNotFoundException when domain not found`() {
        val serviceStore: ServiceStoreInterface = get()
        serviceStore["rocket"] = listOf("start_engine")

        class CustomService :
            ServiceCall(Domain.HOMEASSISTANT, HomeAssistantService.UPDATE_ENTITY) {
            val serviceData = null
        }

        val exception = assertThrows<InstanceCreationException> {
            val bean = khomeModule(createdAtStart = true) { bean { CustomService() } }
            loadKhomeModule(bean)
        }

        assertThat(exception.cause).matchesPredicate { it is DomainNotFoundException }
    }

    @Test
    fun `assert throws ServiceNotFoundException when service not found`() {
        val serviceStore: ServiceStoreInterface = get()
        serviceStore["homeassistant"] = listOf("start_engine")

        class CustomService :
            ServiceCall(Domain.HOMEASSISTANT, HomeAssistantService.UPDATE_ENTITY) {
            val serviceData = null
        }

        val exception = assertThrows<InstanceCreationException> {
            val bean = khomeModule(createdAtStart = true) { bean { CustomService() } }
            loadKhomeModule(bean)
        }

        assertThat(exception.cause).matchesPredicate { it is ServiceNotFoundException }
    }
}
