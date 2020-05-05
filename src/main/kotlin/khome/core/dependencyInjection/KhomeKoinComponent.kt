package khome.core.dependencyInjection

import io.ktor.util.KtorExperimentalAPI
import khome.calling.ServiceCall
import khome.core.entities.EntitySubjectInterface
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.Koin
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
interface KhomeKoinComponent : KoinComponent {
    override fun getKoin(): Koin = checkNotNull(KhomeKoinContext.application) { "No KoinApplication found" }.koin
}

/**
 * Get khome entity instance as singleton
 * @param qualifier
 * @param parameters
 */
inline fun <reified T : EntitySubjectInterface> KhomeKoinComponent.entity(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T = get(qualifier, parameters)

/**
 * Get khome service instance from factory
 * @param qualifier
 * @param parameters
 */
inline fun <reified T : ServiceCall> KhomeKoinComponent.service(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null
): T = get(qualifier, parameters)

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
abstract class KhomeTestComponent : KhomeKoinComponent {

    @BeforeEach
    fun initKoin() {
        KhomeKoinContext.startKoinApplication()
    }

    @AfterEach
    fun closeKoinApplication() {
        KhomeKoinContext.application?.let { it.close() }
    }
}
