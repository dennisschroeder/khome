package khome.core

import khome.Khome.Companion.failureResponseEvents

fun listenCallServiceError(action: ErrorResult.() -> Unit) = failureResponseEvents.subscribe(callback= action)
