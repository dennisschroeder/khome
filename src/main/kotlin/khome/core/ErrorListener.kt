package khome.core

import io.ktor.util.KtorExperimentalAPI
import khome.KhomeSession
import khome.core.events.FailureResponseEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.core.get

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun KhomeSession.listenCallServiceError(action: suspend CoroutineScope.(ResultResponse) -> Unit) =
    get<FailureResponseEvent>().subscribe(callback = action)
