package khome.core.eventHandling

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.google.gson.Gson
import khome.core.ErrorResult
import khome.core.dependencyInjection.KhomeTestComponent
import khome.core.logger
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.get

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FailureResponseEventTest : KhomeTestComponent() {

    @Test
    fun `assert callback was subscribed to event`() {
        val failureResponseEvents = FailureResponseEvent(Event())
        failureResponseEvents.subscribe {
            logger.debug { message }
        }

        assertThat(failureResponseEvents.listenerCount).isEqualTo(1)
    }

    @Test
    fun `assert callback was subscribed to event with handle`() {
        val failureResponseEvents = FailureResponseEvent(Event())
        failureResponseEvents.subscribe("handle") {
            logger.debug { message }
        }

        assertThat(failureResponseEvents.find { it.key == "handle" }).isNotNull()
    }

    @Test
    fun `assert that subscribed event callback was fired`() {
        val failureResponseEvents = FailureResponseEvent(Event())
        var testValue: ErrorResult? = null
        failureResponseEvents.subscribe {
            testValue = this
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

        val errorResult = get<Gson>().fromJson(errorResultJson, ErrorResult::class.java)
        failureResponseEvents.emit(errorResult)

        assertThat(errorResult).isEqualTo(testValue)
    }

    @Test
    fun `assert that subscribed event callbacks were fired`() {
        val failureResponseEvents = FailureResponseEvent(Event())
        var testValueOne: ErrorResult? = null
        var testValueTwo: ErrorResult? = null
        failureResponseEvents.subscribe {
            testValueOne = this
        }

        failureResponseEvents.subscribe("handle") {
            testValueTwo = this
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

        val errorResult = get<Gson>().fromJson(errorResultJson, ErrorResult::class.java)
        failureResponseEvents.emit(errorResult)

        assertThat(errorResult).isEqualTo(testValueOne)
        assertThat(errorResult).isEqualTo(testValueTwo)
    }

    @Test
    fun `assert that unsubscribing was successful`() {
        val failureResponseEvents = FailureResponseEvent(Event())
        var testValueOne: ErrorResult? = null
        var testValueTwo: ErrorResult? = null
        failureResponseEvents.subscribe {
            testValueOne = this
        }

        failureResponseEvents.subscribe("handle") {
            testValueTwo = this
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

        val errorResult = get<Gson>().fromJson(errorResultJson, ErrorResult::class.java)
        failureResponseEvents.unsubscribe("handle")
        failureResponseEvents.emit(errorResult)

        assertThat(errorResult).isEqualTo(testValueOne)
        assertThat(testValueTwo).isNull()
    }
}
