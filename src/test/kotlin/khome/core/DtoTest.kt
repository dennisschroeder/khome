package khome.core

import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEqualTo
import io.ktor.util.KtorExperimentalAPI
import khome.core.dependencyInjection.KhomeTestComponent
import khome.core.entities.getAttribute
import khome.core.exceptions.InvalidAttributeValueTypeException
import khome.core.mapping.ObjectMapper
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.core.get
import java.time.OffsetDateTime

@KtorExperimentalAPI
@ObsoleteCoroutinesApi
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DtoTest : KhomeTestComponent() {

    @Nested
    inner class SuccessResultTest {
        private val resultJson = """
            {
                "id": 13,
                "type": "result",
                "success": true,
                "result": null 
            }
        """.trimIndent()
        private val expectedResult = Result(13, "result", true, null, null)

        @Test
        fun `assert that error response json is correctly mapped`() {
            val result: Result = get<ObjectMapper>().fromJson(resultJson)
            assertThat(result).isEqualTo(expectedResult)
        }
    }

    @Nested
    inner class ErrorResultTest {

        @Test
        fun `assert that json is correctly mapped`() {
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

            val errorResult: Result = get<ObjectMapper>().fromJson(errorResultJson)

            val expectedErrorResponse =
                Result(
                    12,
                    "result",
                    false,
                    ErrorResult(
                        "2",
                        "Message incorrectly formatted: expected str for dictionary value @ data['event_type']. Got 100"
                    ),
                    null
                )

            assertThat(errorResult).isEqualTo(expectedErrorResponse)
        }
    }

    @Nested
    inner class StateResultTest {

        private val resultJson = """
            {
                "id": 13,
                "type": "result",
                "success": true,
                "result": [
                    {
                        "entity_id":"light.bed_light",
                        "last_changed":"2016-11-26T01:37:24.265390Z",
                        "state":"on",
                        "attributes":{
                           "rgb_color":[
                              254,
                              208,
                              0
                           ],
                           "color_temp":380,
                           "supported_features":147,
                           "xy_color":[
                              0.5,
                              0.5
                           ],
                           "brightness":180,
                           "white_value":200,
                           "friendly_name":"Bed Light"
                        },
                        "last_updated":"2016-11-26T01:37:24.265390Z"
                     }
                ] 
            }
        """.trimIndent()

        // Note that [Gson] can not defer between Int, Floats and Doubles in maps.
        // Therefore we need to expect all Ints in Maps as Double/Float values.
        private val attributes = mapOf(
            "rgb_color" to listOf(254.0, 208.0, 0.0),
            "color_temp" to 380.0,
            "supported_features" to 147.0,
            "xy_color" to listOf(0.5, 0.5),
            "brightness" to 180.0,
            "white_value" to 200.0,
            "friendly_name" to "Bed Light"
        )
        private val state =
            State("light.bed_light", OffsetDateTime.parse("2016-11-26T01:37:24.265390Z"), "on", attributes, OffsetDateTime.parse("2016-11-26T01:37:24.265390Z"))

        private val expectedResult = StateResult(13, "result", true, arrayOf(state))

        @Test
        fun `assert that error response json is correctly mapped`() {
            val result: StateResult = get<ObjectMapper>().fromJson(resultJson)
            assertThat(result.result).isEqualTo(expectedResult.result)
        }
    }

    @Nested
    inner class EventResultTest {
        // Note that [Gson] can not defer between Int, Floats and Doubles in maps.
        // Therefore we need to expect all Ints in Maps as Double/Float values.
        private val newAttributes = mapOf(
            "rgb_color" to listOf(254.0, 208.0, 0.0),
            "color_temp" to 380.0,
            "supported_features" to 147.0,
            "xy_color" to listOf(0.5, 0.5),
            "brightness" to 180.0,
            "white_value" to 200.0,
            "friendly_name" to "Bed Light"
        )

        private val oldAttributes = mapOf("supported_features" to 147.0, "friendly_name" to "Bed Light")
        private val newState =
            State("light.bed_light", OffsetDateTime.parse("2016-11-26T01:37:24.265390Z"), "on", newAttributes, OffsetDateTime.parse("2016-11-26T01:37:24.265390Z"))
        private val oldState =
            State("light.bed_light", OffsetDateTime.parse("2016-11-26T01:37:10.466994Z"), "off", oldAttributes, OffsetDateTime.parse("2016-11-26T01:37:10.466994Z"))
        private val data = Data("light.bed_light", oldState, newState)
        private val event = Event("state_changed", data, OffsetDateTime.parse("2016-11-26T01:37:24.265429Z"), "LOCAL")
        private val expectedResult = EventResult(18, "event", event)

        @Test
        fun `assert that json is correctly mapped`() {
            val eventResultJson = """
            {
               "id": 18,
               "type":"event",
               "event":{
                  "data":{
                     "entity_id":"light.bed_light",
                     "new_state":{
                        "entity_id":"light.bed_light",
                        "last_changed":"2016-11-26T01:37:24.265390+00:00",
                        "state":"on",
                        "attributes":{
                           "rgb_color":[
                              254,
                              208,
                              0
                           ],
                           "color_temp":380,
                           "supported_features":147,
                           "xy_color":[
                              0.5,
                              0.5
                           ],
                           "brightness":180,
                           "white_value":200,
                           "friendly_name":"Bed Light"
                        },
                        "last_updated":"2016-11-26T01:37:24.265390+00:00"
                     },
                     "old_state":{
                        "entity_id":"light.bed_light",
                        "last_changed":"2016-11-26T01:37:10.466994+00:00",
                        "state":"off",
                        "attributes":{
                           "supported_features":147,
                           "friendly_name":"Bed Light"
                        },
                        "last_updated":"2016-11-26T01:37:10.466994+00:00"
                     }
                  },
                  "event_type":"state_changed",
                  "time_fired":"2016-11-26T01:37:24.265429+00:00",
                  "origin":"LOCAL"
               }
            }
        """.trimIndent()
            val eventResult: EventResult = get<ObjectMapper>().fromJson(eventResultJson)
            assertThat(eventResult).isDataClassEqualTo(expectedResult)
        }

        @Test
        fun `assert that getAttribute returns correct type and value`() {
            val stateAttribute = newState.getAttribute<List<Double>>("rgb_color")
            assertThat(stateAttribute).isEqualTo(listOf(254.0, 208.0, 0.0))
        }

        @Test
        fun `assert getAttribute throws InvalidAttributeValueTypeException on wrong Type parameter`() {
            val exception = assertThrows<InvalidAttributeValueTypeException> { newState.getAttribute<Boolean>("color_temp") }
            assertThat(exception.message).isEqualTo("Attribute value for color_temp is of type: ${Double::class}.")
        }
    }

    @Nested
    inner class ServiceResultTest {
        private val entityIdField = mapOf(
            "entity_id" to mapOf(
                "description" to "The entity_id of the device.",
                "example" to "light.living_room"
            )
        )
        private val turnOnDescription = mapOf(
            "description" to "Generic service to turn devices on under any domain. Same usage as the light.turn_on, switch.turn_on, etc. services.",
            "fields" to entityIdField
        )
        private val turnOffDescription = mapOf(
            "description" to "Generic service to turn devices off under any domain. Same usage as the light.turn_on, switch.turn_on, etc. services.",
            "fields" to entityIdField
        )

        private val expectedResult = ServiceResult(
            19,
            "result",
            success = true,
            result = mapOf("turn_on" to turnOnDescription, "turn_off" to turnOffDescription)
        )
        @Test
        fun `assert that json is correctly mapped`() {
            val serviceResultJson = """
                {
                   "id":19,
                   "type":"result",
                   "success":true,
                   "result":{
                      "turn_off":{
                         "description":"Generic service to turn devices off under any domain. Same usage as the light.turn_on, switch.turn_on, etc. services.",
                         "fields":{
                            "entity_id":{
                               "description":"The entity_id of the device.",
                               "example":"light.living_room"
                            }
                         }
                      },
                      "turn_on":{
                         "description":"Generic service to turn devices on under any domain. Same usage as the light.turn_on, switch.turn_on, etc. services.",
                         "fields":{
                            "entity_id":{
                               "description":"The entity_id of the device.",
                               "example":"light.living_room"
                            }
                         }
                      }
                   }
                }
            """.trimIndent()
            val serviceResult: ServiceResult = get<ObjectMapper>().fromJson(serviceResultJson)
            assertThat(serviceResult).isDataClassEqualTo(expectedResult)
        }
    }
}
