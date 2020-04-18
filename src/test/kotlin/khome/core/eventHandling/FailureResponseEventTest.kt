package khome.core.eventHandling

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.google.gson.Gson
import khome.core.Result
import khome.core.dependencyInjection.KhomeTestComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.get

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FailureResponseEventTest : KhomeTestComponent() {
    private val logger = KotlinLogging.logger { }

    @Test
    fun `assert callback was subscribed to event`() {
        val failureResponseEvents = FailureResponseEvent(Event())
        failureResponseEvents.subscribe {
            logger.debug { it.error!!.message }
        }

        assertThat(failureResponseEvents.listenerCount).isEqualTo(1)
    }

    @Test
    fun `assert callback was subscribed to event with handle`() {
        val failureResponseEvents = FailureResponseEvent(Event())
        failureResponseEvents.subscribe("handle") {
            logger.debug { it.error!!.message }
        }

        assertThat(failureResponseEvents.find { it.key == "handle" }).isNotNull()
    }

    @Test
    fun `assert that subscribed event callback was fired`() = runBlocking {
        val failureResponseEvents = FailureResponseEvent(Event())
        var testValue: Result? = null
        failureResponseEvents.subscribe {
            testValue = it
        }

        val errorResultJson = """
            {
               "id": 12,
               "type":"result",
               "success": false,
               "error": {
                  "code": 2,
                  "message": "Message incorrectly formatted: expected str for dictionary value @ data['event_type']. Got 100"
               }
            }
        """.trimIndent()

        val errorResult = get<Gson>().fromJson(errorResultJson, Result::class.java)
        failureResponseEvents.emit(errorResult)
        delay(10)
        assertThat(errorResult).isEqualTo(testValue)
    }

    @Test
    fun `assert that subscribed event callbacks were fired`() = runBlocking {
        val failureResponseEvents = FailureResponseEvent(Event())
        var testValueOne: Result? = null
        var testValueTwo: Result? = null
        failureResponseEvents.subscribe {
            testValueOne = it
        }

        failureResponseEvents.subscribe("handle") {
            testValueTwo = it
        }

        val errorResultJson = """
            {
               "id": 12,
               "type":"result",
               "success": false,
               "error": {
                  "code": 2,
                  "message": "Message incorrectly formatted: expected str for dictionary value @ data['event_type']. Got 100"
               }
            }
        """.trimIndent()

        val errorResult: Result = get<Gson>().fromJson(errorResultJson, Result::class.java)
        failureResponseEvents.emit(errorResult)
        delay(10)
        assertThat(errorResult).isEqualTo(testValueOne)
        assertThat(errorResult).isEqualTo(testValueTwo)
    }

    @Test
    fun `assert that unsubscribe was successful`() = runBlocking {
        val failureResponseEvents = FailureResponseEvent(Event())
        var testValueOne: Result? = null
        var testValueTwo: Result? = null
        failureResponseEvents.subscribe {
            testValueOne = it
        }

        failureResponseEvents.subscribe("handle") {
            testValueTwo = it
        }

        val errorResultJson = """
            {
               "id": 12,
               "type":"result",
               "success": false,
               "error": {
                  "code": 2,
                  "message": "Message incorrectly formatted: expected str for dictionary value @ data['event_type']. Got 100"
               }
            }
        """.trimIndent()

        val errorResult = get<Gson>().fromJson(errorResultJson, Result::class.java)
        failureResponseEvents.unsubscribe("handle")
        failureResponseEvents.emit(errorResult)
        delay(10)
        assertThat(errorResult).isEqualTo(testValueOne)
        assertThat(testValueTwo).isNull()
    }
}
