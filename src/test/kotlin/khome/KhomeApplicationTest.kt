package khome

import assertk.assertThat
import assertk.assertions.isInstanceOf
import khome.communicating.DefaultResolvedServiceCommand
import khome.communicating.EntityIdOnlyServiceData
import khome.communicating.ServiceCommandResolver
import khome.communicating.ServiceType
import khome.core.Attributes
import khome.core.State
import khome.core.koin.KhomeKoinContext
import khome.entities.EntityId
import khome.entities.devices.Actuator
import khome.entities.devices.Sensor
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Instant

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KhomeApplicationTest {

    val sut = KhomeApplicationImpl()

    @BeforeAll
    fun startKoin() {
        KhomeKoinContext.startKoinApplication()
    }

    data class SensorState(override val value: String) : State<String>

    data class SensorAttributes(
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
                    EntityId("sensor", "some_sensor"),
                    SensorState::class,
                    SensorAttributes::class
                )

            assertThat(sensor).isInstanceOf(Sensor::class)
        }
    }

    data class ActuatorState(override val value: String) : State<String>
    data class ActuatorAttributes(
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
                    EntityId("light", "some_light"),
                    SensorState::class,
                    SensorAttributes::class,
                    ServiceCommandResolver { DefaultResolvedServiceCommand(ServiceType.TURN_ON, EntityIdOnlyServiceData()) }
                )

            assertThat(actuator).isInstanceOf(Actuator::class)
        }
    }
}
