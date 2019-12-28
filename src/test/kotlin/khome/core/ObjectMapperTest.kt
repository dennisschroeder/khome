package khome.core

import assertk.assertThat
import assertk.assertions.isNotNull
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import io.ktor.util.KtorExperimentalAPI
import khome.core.dependencyInjection.KhomeTestComponent
import khome.core.mapping.ObjectMapper
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.core.get
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.time.OffsetDateTime

data class TestData(val text: String, val number: Int, val state: Boolean, val dateTime: OffsetDateTime?)

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ObjectMapperTest : KhomeTestComponent() {
    private val someJson = """
            {
                "text": "some text about whatever",
                "number": 73,
                "state": true,
                "date_time": "2019-11-16T19:39:31.497638Z"
            }
        """.trimIndent()

    @Test
    fun `assert ObjectMapper reads correctly`() {
        val mapper: ObjectMapper = get()
        val testDataFromJson: TestData = mapper.fromJson(someJson)
        val testDataFromObject = mapper.toJson(testDataFromJson)

        JSONAssert.assertEquals(testDataFromObject, someJson, JSONCompareMode.STRICT)
    }

    @Test
    fun `assert throws IllegalStateException when missing comma`() {
        val malformedJson = """
               {
                "text": "some text about whatever",
                "number": 73
                "state": true,
                "date_time": "2019-11-16T19:39:31.497638Z"
            }
            """.trimIndent()

        val exception = assertThrows<JsonSyntaxException> {
            get<ObjectMapper>().fromJson<TestData>(malformedJson)
        }
        assertThat(exception.cause).isNotNull()
        assertThat { exception.cause is MalformedJsonException }
    }

    @Test
    fun `assert throws IllegalStateException when value does not fit expected type`() {
        val malformedJson = """
               {
                "text": "some text about whatever",
                "number": true,
                "state": true,
                "date_time": "2019-11-16T19:39:31.497638Z"
            }
            """.trimIndent()
        val exception = assertThrows<JsonSyntaxException> { get<ObjectMapper>().fromJson<TestData>(malformedJson) }
        assertThat(exception.cause).isNotNull()
        assertThat { exception.cause is IllegalStateException }
    }
}
