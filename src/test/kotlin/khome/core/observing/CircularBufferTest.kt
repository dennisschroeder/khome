package khome.core.observing

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CircularBufferTest {

    @Test
    fun `youngest element first in order`() {
        val sut = CircularBuffer<Int>(10)

        (1..10).forEach {
            sut.addFirst(it)
        }

        assertThat(sut.snapshot[0]).isEqualTo(10)
        assertThat(sut.snapshot[9]).isEqualTo(1)
    }

    @Test
    fun `max capacity constraints buffer size`() {
        val sut = CircularBuffer<Int>(100)

        (1..100).forEach {
            sut.addFirst(it)
        }

        assertThrows<IndexOutOfBoundsException> {
            sut.snapshot[100]
        }
    }

    @Test
    fun `last() returns null on empty buffer`() {
        val sut = CircularBuffer<Int>(10)
        assertThat(sut.first).isNull()
        assertThat(sut.last).isNull()
    }
}
