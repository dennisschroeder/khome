package khome

import assertk.assertThat
import assertk.assertions.isSuccess
import io.ktor.util.KtorExperimentalAPI
import khome.core.ConfigurationInterface
import khome.testing.KhomeTest
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KhomeTesting : KhomeTest() {

    init {
        beans {
            single<ConfigurationInterface>(override = true) { TestConfiguration() }
        }
    }

    @Test
    fun `When startKhome an Khome instance is returned`() {

        assertThat { "input_boolean.dev_mode" in stateStore }.isSuccess()
    }
}
