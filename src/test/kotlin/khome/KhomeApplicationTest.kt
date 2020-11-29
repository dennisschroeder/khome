package khome

import assertk.assertThat
import assertk.assertions.isInstanceOf
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.core.koin.KhomeKoinContext
import khome.entities.Attributes
import khome.entities.State
import khome.entities.devices.Actuator
import khome.entities.devices.Sensor
import khome.values.EntityId
import khome.values.UserId
import khome.values.domain
import khome.values.objectId
import khome.values.service
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Instant

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KhomeApplicationTest {

    val sut = khomeApplication()

    data class SensorState(override val value: String) : State<String>

    data class SensorAttributes(
        override val userId: UserId?,
        override val lastChanged: Instant,
        override val lastUpdated: Instant,
        override val friendlyName: String
    ) : Attributes

    @Nested
    inner class Sensors {

        @Test
        fun `assert sensor factory creates new Sensor instance`() {
            val sensor =
                sut.Sensor<SensorState, SensorAttributes>(
                    EntityId.fromPair("sensor".domain to "some_sensor".objectId),
                    SensorState::class,
                    SensorAttributes::class
                )

            assertThat(sensor).isInstanceOf(Sensor::class)
        }
    }

    data class ActuatorState(override val value: String) : State<String>
    data class ActuatorAttributes(
        override val userId: UserId?,
        override val lastChanged: Instant,
        override val lastUpdated: Instant,
        override val friendlyName: String
    ) : Attributes

    @Nested
    inner class Actuators {
        @Test
        fun `assert actuator factory creates new Actuator instance`() {
            val actuator =
                sut.Actuator<ActuatorState, ActuatorAttributes>(
                    EntityId.fromPair("light".domain to "some_light".objectId),
                    SensorState::class,
                    SensorAttributes::class,
                    ServiceCommandResolver {
                        DefaultResolvedServiceCommand(
                            null,
                            "turn_on".service,
                            EntityIdOnlyServiceData()
                        )
                    }
                )

            assertThat(actuator).isInstanceOf(Actuator::class)
        }
    }

    @AfterAll
    fun stopKoin() {
        KhomeKoinContext.application?.close()
    }
}
