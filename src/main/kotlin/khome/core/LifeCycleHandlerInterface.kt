package khome.core

interface LifeCycleHandlerInterface {
    val lazyCancellation: Unit
    fun cancel()
    fun cancelInSeconds(seconds: Int): LifeCycleHandlerInterface
    fun cancelInMinutes(minutes: Int): LifeCycleHandlerInterface
}