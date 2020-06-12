package khome.communicating

import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext

class ServiceCoroutineContext : CoroutineContext by newSingleThreadContext("ServiceContext")
