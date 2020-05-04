package khome.core.boot

import khome.KhomeSession

internal interface BootSequenceInterface {
    val khomeSession: KhomeSession
    suspend fun runBootSequence()
}
