package khome.core

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.core.eventHandling.FailureResponseEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun KhomeSession.listenCallServiceError(action: suspend CoroutineScope.(Result) -> Unit) =
    get<FailureResponseEvent>().subscribe(callback = action)
