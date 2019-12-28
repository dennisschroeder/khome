package khome.calling

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSuccess
import assertk.assertions.matchesPredicate
import khome.calling.errors.DomainNotFoundException
import khome.calling.errors.ServiceNotFoundException
import khome.core.ServiceStoreInterface
import khome.core.dependencyInjection.KhomeTestComponent
import khome.core.dependencyInjection.loadKhomeModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.core.error.InstanceCreationException
import org.koin.core.get
import org.koin.dsl.module

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ServiceCallTest : KhomeTestComponent() {

    @Test
    fun `happy path - create service call through dependency injection`() {
        val serviceStore: ServiceStoreInterface = get()
        serviceStore["homeassistant"] = listOf("update_entity")

        class CustomService :
            ServiceCall(Domain.HOMEASSISTANT, HomeAssistantServices.UPDATE_ENTITY, EntityId("foo.bar"))

        assertThat {
            val bean = module(createdAtStart = true) { single { CustomService() } }
            loadKhomeModule(bean).createEagerInstances()
        }.isSuccess()

        val customService: CustomService = get()
        assertThat(customService.domain).isEqualTo(Domain.HOMEASSISTANT)
        assertThat(customService.service).isEqualTo(HomeAssistantServices.UPDATE_ENTITY)
        assertThat(customService.serviceData).isEqualTo(EntityId("foo.bar"))
    }

    @Test
    fun `assert throws DomainNotFoundException when domain not found`() {
        val serviceStore: ServiceStoreInterface = get()
        serviceStore["rocket"] = listOf("start_engine")

        class CustomService :
            ServiceCall(Domain.HOMEASSISTANT, HomeAssistantServices.UPDATE_ENTITY, EntityId("foo.bar"))

        val exception = assertThrows<InstanceCreationException> {
            val bean = module(createdAtStart = true) { single { CustomService() } }
            loadKhomeModule(bean).createEagerInstances()
        }

        assertThat(exception.cause).matchesPredicate { it is DomainNotFoundException }
    }

    @Test
    fun `assert throws ServiceNotFoundException when service not found`() {
        val serviceStore: ServiceStoreInterface = get()
        serviceStore["homeassistant"] = listOf("start_engine")

        class CustomService :
            ServiceCall(Domain.HOMEASSISTANT, HomeAssistantServices.UPDATE_ENTITY, EntityId("foo.bar"))

        val exception = assertThrows<InstanceCreationException> {
            val bean = module(createdAtStart = true) { single { CustomService() } }
            loadKhomeModule(bean).createEagerInstances()
        }

        assertThat(exception.cause).matchesPredicate { it is ServiceNotFoundException }
    }
}
