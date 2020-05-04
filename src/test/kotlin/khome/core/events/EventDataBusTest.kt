package khome.core.events

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventDataBusTest {
    private val logger = KotlinLogging.logger { }

    @Test
    fun `assert callback was added with plus assign`() {
        val testEvent = Event<String>()
        testEvent += {
            logger.debug { it }
        }
        assertThat(testEvent.size).isEqualTo(1)
    }

    @Test
    fun `assert callback was added`() {
        val testEvent = Event<String>()
        testEvent += {
            logger.debug { it }
        }
        assertThat(testEvent.size).isEqualTo(1)
    }

    @Test
    fun `assert that callback was executed when event was fired`() = runBlocking {
        val testEvent = Event<String>()
        var testValue: String? = null
        testEvent += {
            testValue = it
        }

        testEvent.collect { handler ->
            handler("Foo")
        }
        delay(1)
        assertThat(testValue).isEqualTo("Foo")
    }

    @Test
    fun `assert that all callbacks were executed when event was fired`() = runBlocking {
        val testEvent = Event<String>()
        var testValueOne: String? = null
        var testValueTwo: String? = null
        testEvent += {
            logger.debug { it }
            testValueOne = it
        }

        testEvent["handler"] = {
            testValueTwo = it
        }

        testEvent.collect { handler ->
            handler("Foo")
        }
        delay(10)
        assertThat(testValueOne).isEqualTo("Foo")
        assertThat(testValueTwo).isEqualTo("Foo")
    }

    @Test
    fun `assert that callback was added with handle`() {
        val testEvent = Event<String>()
        testEvent["handle"] = {
            logger.debug { it }
        }

        assertThat(testEvent.listeners.find { it.key == "handle" }).isNotNull()
    }

    @Test
    fun `assert that callback was removed by handle`() {
        val testEvent = Event<String>()
        testEvent["handle"] = {
            logger.debug { it }
        }

        testEvent -= "handle"
        assertThat(testEvent.listeners.find { it.key == "handle" }).isNull()
    }

    @Test
    fun `assert that all callbacks were removed`() {
        val testEvent = Event<String>()
        testEvent["handle"] = {
            logger.debug { it }
        }

        testEvent += {
            logger.debug { it }
        }

        testEvent.clear()
        assertThat(testEvent.listeners).isEmpty()
    }
}