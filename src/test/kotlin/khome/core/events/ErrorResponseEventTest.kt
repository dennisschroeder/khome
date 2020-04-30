package khome.core.events

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.google.gson.Gson
import khome.core.ResultResponse
import khome.core.dependencyInjection.KhomeTestComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.get

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ErrorResponseEventTest : KhomeTestComponent() {
    private val logger = KotlinLogging.logger { }

    @Test
    fun `assert callback was subscribed to event`() {
        val failureResponseEvents = ErrorResponseEvent(Event())
        failureResponseEvents.subscribe {
            logger.debug { it.error!!.message }
        }

        assertThat(failureResponseEvents.listenerCount).isEqualTo(1)
    }

    @Test
    fun `assert callback was subscribed to event with handle`() {
        val failureResponseEvents = ErrorResponseEvent(Event())
        failureResponseEvents.subscribe("handle") {
            logger.debug { it.error!!.message }
        }

        assertThat(failureResponseEvents.find { it.key == "handle" }).isNotNull()
    }

    @Test
    fun `assert that subscribed event callback was fired`() = runBlocking {
        val failureResponseEvents = ErrorResponseEvent(Event())
        var testValue: ResultResponse? = null
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

        val errorResult = get<Gson>().fromJson(errorResultJson, ResultResponse::class.java)
        failureResponseEvents.emit(errorResult)
        delay(10)
        assertThat(errorResult).isEqualTo(testValue)
    }

    @Test
    fun `assert that subscribed event callbacks were fired`() = runBlocking {
        val failureResponseEvents = ErrorResponseEvent(Event())
        var testValueOne: ResultResponse? = null
        var testValueTwo: ResultResponse? = null
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

        val errorResultResponse: ResultResponse = get<Gson>().fromJson(errorResultJson, ResultResponse::class.java)
        failureResponseEvents.emit(errorResultResponse)
        delay(10)
        assertThat(errorResultResponse).isEqualTo(testValueOne)
        assertThat(errorResultResponse).isEqualTo(testValueTwo)
    }

    @Test
    fun `assert that unsubscribe was successful`() = runBlocking {
        val failureResponseEvents = ErrorResponseEvent(Event())
        var testValueOne: ResultResponse? = null
        var testValueTwo: ResultResponse? = null
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

        val errorResult = get<Gson>().fromJson(errorResultJson, ResultResponse::class.java)
        failureResponseEvents.unsubscribe("handle")
        failureResponseEvents.emit(errorResult)
        delay(10)
        assertThat(errorResult).isEqualTo(testValueOne)
        assertThat(testValueTwo).isNull()
    }
}
