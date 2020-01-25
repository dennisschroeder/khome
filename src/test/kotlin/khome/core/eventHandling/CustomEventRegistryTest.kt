package khome.core.eventHandling

import assertk.assertThat
import assertk.assertions.isSuccess
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CustomEventRegistryTest {

    @Test
    fun `operator plus adds event`() {
        val sut = CustomEventRegistry()
        sut.register("testEvent")

        assertThat("testEvent" in sut)
    }

    @Test
    fun `can iterate over registry`() {
        val sut = CustomEventRegistry()
        sut.register("testEventOne")
        sut.register("testEventTwo")

        assertThat {
            sut.forEach { event ->
                assertThat(event in sut)
            }
        }.isSuccess()
    }
}
