package khome.entities.devices

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.google.gson.JsonObject
import khome.KhomeApplicationImpl
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.core.boot.statehandling.flattenStateAttributes
import khome.core.koin.KhomeKoinContext
import khome.core.koin.KoinContainer
import khome.core.mapping.ObjectMapperInterface
import khome.core.mapping.fromJson
import khome.entities.Attributes
import khome.entities.State
import khome.khomeApplication
import khome.values.FriendlyName
import khome.values.UserId
import khome.values.service
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.koin.core.component.get
import java.time.Instant
import java.time.OffsetDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ActuatorTest {

    data class ActuatorTestState(override val value: String, val booleanAttribute: Boolean, val intAttribute: Int) :
        State<String>

    data class ActuatorTestAttributes(
        val arrayAttribute: List<Int>,
        val doubleAttribute: Double,
        override val userId: UserId?,
        override val lastChanged: Instant,
        override val lastUpdated: Instant,
        override val friendlyName: FriendlyName
    ) : Attributes

    @BeforeAll
    fun createKhome() {
        khomeApplication()
    }

    private val mapper: ObjectMapperInterface
        get() = KoinContainer.get()

    @Test
    fun `actuator state response mapping is correct`() {

        val sut = ActuatorImpl<ActuatorTestState, ActuatorTestAttributes>(
            app = KhomeApplicationImpl(),
            mapper = mapper,
            resolver = ServiceCommandResolver {
                DefaultResolvedServiceCommand(
                    null,
                    "turn_on".service,
                    EntityIdOnlyServiceData()
                )
            },
            stateType = ActuatorTestState::class,
            attributesType = ActuatorTestAttributes::class
        )

        assertThrows<IllegalStateException> {
            sut.actualState
        }

        val testStateJson =
            //language=json
            """
                {
                    "entity_id":"test.object_id",
                    "last_changed":"2016-11-26T01:37:24.265390+00:00",
                    "state":"on",
                    "attributes":{
                        "array_attribute": [1,2,3,4,5],
                        "int_attribute": 73,
                        "double_attribute": 30.0,
                        "boolean_attribute": true,
                        "friendly_name":"Test Entity"
                    },
                    "last_updated":"2016-11-26T01:37:24.265390+00:00",
                    "context": { "user_id": null }
                 }
            """.trimIndent()

        val stateAsJsonObject = mapper.fromJson<JsonObject>(testStateJson)

        sut.trySetAttributesFromAny(flattenStateAttributes(stateAsJsonObject))
        sut.trySetActualStateFromAny(flattenStateAttributes(stateAsJsonObject))

        assertThat(sut.actualState.value).isEqualTo("on")
        assertThat(sut.actualState.booleanAttribute).isEqualTo(true)
        assertThat(sut.actualState.intAttribute).isEqualTo(73)
        assertThat(sut.attributes.arrayAttribute).isEqualTo(listOf(1, 2, 3, 4, 5))
        assertThat(sut.attributes.doubleAttribute).isEqualTo(30.0)
        assertThat(sut.attributes.friendlyName).isEqualTo(FriendlyName.from("Test Entity"))
        assertThat(sut.attributes.lastChanged).isEqualTo(
            OffsetDateTime.parse("2016-11-26T01:37:24.265390+00:00").toInstant()
        )
        assertThat(sut.attributes.lastUpdated).isEqualTo(
            OffsetDateTime.parse("2016-11-26T01:37:24.265390+00:00").toInstant()
        )
    }

    @Test
    fun `actuator stores state and attributes youngest first`() {
        val sut = ActuatorImpl<ActuatorTestState, ActuatorTestAttributes>(
            app = KhomeApplicationImpl(),
            mapper = mapper,
            resolver = ServiceCommandResolver {
                DefaultResolvedServiceCommand(
                    null,
                    "turn_on".service,
                    EntityIdOnlyServiceData()
                )
            },
            stateType = ActuatorTestState::class,
            attributesType = ActuatorTestAttributes::class
        )

        val firstTestState =
            //language=json
            """ 
                {
                    "entity_id":"test.object_id",
                    "last_changed":"2016-11-26T01:37:24.265390+00:00",
                    "state":"off",
                    "attributes":{
                        "array_attribute": [1,2,3,4,5],
                        "int_attribute": 73,
                        "double_attribute": 30.0,
                        "boolean_attribute": true,
                        "friendly_name":"Test Entity"
                    },
                    "last_updated":"2016-11-26T01:37:24.265390+00:00",
                    "context": { "user_id": null }
                 }
            """.trimIndent()

        val secondTestState =
            //language=json
            """
                {
                    "entity_id":"test.object_id",
                    "last_changed":"2016-11-26T01:37:24.265390+00:00",
                    "state":"on",
                    "attributes":{
                        "array_attribute": [1,2,3,4,5],
                        "int_attribute": 73,
                        "double_attribute": 30.0,
                        "boolean_attribute": true,
                        "friendly_name":"Test Entity"
                    },
                    "last_updated":"2016-11-26T01:37:24.265390+00:00",
                    "context": { "user_id": null }
                 }
            """.trimIndent()

        val firstStateAsJsonObject = mapper.fromJson<JsonObject>(firstTestState)

        sut.trySetAttributesFromAny(flattenStateAttributes(firstStateAsJsonObject))
        sut.trySetActualStateFromAny(flattenStateAttributes(firstStateAsJsonObject))

        assertThat(sut.history.size).isEqualTo(1)
        assertThat(sut.actualState).isEqualTo(sut.history.first().state)
        assertThat(sut.actualState.value).isEqualTo("off")

        val secondStateAsJsonObject = mapper.fromJson<JsonObject>(secondTestState)

        sut.trySetAttributesFromAny(flattenStateAttributes(secondStateAsJsonObject))
        sut.trySetActualStateFromAny(flattenStateAttributes(secondStateAsJsonObject))

        assertThat(sut.history.size).isEqualTo(2)
        assertThat(sut.actualState).isEqualTo(sut.history.first().state)
        assertThat(sut.history[1].state.value).isEqualTo("off")
    }

    @AfterAll
    fun stopKoin() {
        KhomeKoinContext.application?.close()
    }
}
