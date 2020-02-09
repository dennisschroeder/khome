package khome.calling

import kotlin.coroutines.CoroutineContext

class ServiceCoroutineContext(delegate: CoroutineContext) : CoroutineContext by delegate
