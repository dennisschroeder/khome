package khome.core

import khome.Khome.Companion.subscribeErrorResultEvent

fun listenCallServiceError(action: ErrorResult.() -> Unit) = subscribeErrorResultEvent(callback= action)