package khome.core

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.core.eventHandling.FailureResponseEvents
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun KhomeSession.listenCallServiceError(action: ErrorResult.() -> Unit) =
    get<FailureResponseEvents>().subscribe(callback = action)
