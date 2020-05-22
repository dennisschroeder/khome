package khome.calling

import kotlinx.coroutines.newSingleThreadContext
import kotlin.coroutines.CoroutineContext

class ServiceCoroutineContext : CoroutineContext by newSingleThreadContext("ServiceContext")
