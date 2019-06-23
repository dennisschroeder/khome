package khome

import khome.core.State
import khome.scheduling.toDate
import org.junit.jupiter.api.Test
import java.time.LocalDateTime


class StateComparisonTest {

    @Test
    fun compareStateObjects() {
        val date = LocalDateTime.now()

        val stateOne = State(
            entityId = "domain.id",
            lastChanged = date.toDate(),
            state = "on",
            attributes = mapOf("foo" to "bar", "bar" to "baz")
        )
        val stateTwo = State(
            entityId = "domain.id",
            lastChanged = date.toDate(),
            state = "on",
            attributes = mapOf("foo" to "bar", "bar" to "baz")
        )

        assert(stateOne == stateTwo)
    }
}