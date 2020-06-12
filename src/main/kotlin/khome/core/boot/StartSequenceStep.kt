package khome.core.boot

import khome.KhomeSession

internal interface StartSequenceStep {
    val khomeSession: KhomeSession
    suspend fun runStartSequenceStep()
}
