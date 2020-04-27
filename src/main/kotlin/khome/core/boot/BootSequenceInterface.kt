package khome.core.boot

import khome.KhomeSession

interface BootSequenceInterface {
    val khomeSession: KhomeSession
    suspend fun runBootSequence()
}
