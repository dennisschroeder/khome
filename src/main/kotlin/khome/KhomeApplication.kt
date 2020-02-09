package khome

import io.ktor.util.KtorExperimentalAPI
import khome.core.BaseKhomeComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.coroutineScope
import org.koin.core.get

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
@ObsoleteCoroutinesApi
class KhomeApplication(private val khomeClient: KhomeClient) {

    suspend fun runApplication(listeners: suspend BaseKhomeComponent.() -> Unit = {}) =
        coroutineScope {
            khomeClient.startSession {
                    initiateApplication(get(), listeners)
                }
        }
}
