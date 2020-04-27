package khome.core.events

import assertk.assertThat
import assertk.assertions.isSuccess
import khome.core.dependencyInjection.KhomeTestComponent
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class HassEventRegistryTest : KhomeTestComponent() {

    @Test
    fun `operator plus adds event`() {
        val sut = HassEventRegistry()
        class MyEvent : HassEvent("testEvent")

        sut.register("testEvent", MyEvent())

        assertThat("testEvent" in sut)
    }

    @Test
    fun `can iterate over registry`() {
        val sut = HassEventRegistry()
        class MyFirstEvent : HassEvent("testEventOne")
        class MySecondEvent : HassEvent("testEventTwo")
        sut.register("testEventOne", MyFirstEvent())
        sut.register("testEventTwo", MySecondEvent())

        assertThat {
            runBlocking {
                sut.forEach { event ->
                    assertThat(event.key in sut)
                }
            }
        }.isSuccess()
    }
}
