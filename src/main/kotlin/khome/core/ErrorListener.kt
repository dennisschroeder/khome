package khome.core

import khome.core.dependencyInjection.KhomeKoinComponent
import khome.core.eventHandling.FailureResponseEvents
import org.koin.core.get

fun listenCallServiceError(action: ErrorResult.() -> Unit) = object : KhomeKoinComponent() {}.get<FailureResponseEvents>().subscribe(callback = action)
