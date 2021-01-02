package khome.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext

object KhomeDispatchers {
    internal val CommandDispatcher = newSingleThreadContext("command-dispatcher")
    val Unconfined = Dispatchers.Unconfined
    val SingleThread = newSingleThreadContext("single-thread-dispatcher")
    val CPUBound = Dispatchers.Default
    val IOBound = Dispatchers.IO
}
