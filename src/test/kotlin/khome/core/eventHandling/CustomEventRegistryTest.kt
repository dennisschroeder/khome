package khome.core.eventHandling

import assertk.assertThat
import assertk.assertions.isSuccess
import khome.core.dependencyInjection.KhomeTestComponent
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CustomEventRegistryTest : KhomeTestComponent() {

    @Test
    fun `operator plus adds event`() {
        val sut = CustomEventRegistry()
        class MyEvent : CustomEvent("testEvent")

        sut.register("testEvent", MyEvent())

        assertThat("testEvent" in sut)
    }

    @Test
    fun `can iterate over registry`() {
        val sut = CustomEventRegistry()
        class MyFirstEvent : CustomEvent("testEventOne")
        class MySecondEvent : CustomEvent("testEventTwo")
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
