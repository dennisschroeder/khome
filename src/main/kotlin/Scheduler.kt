package khome

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.*

inline fun runEveryAt(period: Long, time: Date, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val timer = fixedRateTimer(handle, true, time, period, action)

    return LifeCycleHandler(timer)
}

inline fun runDailyAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {

    val startDate = createDateFromTimeOfDayAsString(timeOfDay)
    val periodInMilliseconds = TimeUnit.DAYS.toMillis(1)

    return runEveryAt(periodInMilliseconds, startDate, action)
}

inline fun runHourlyAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createDateFromTimeOfDayAsString(timeOfDay)
    val periodInMilliseconds = TimeUnit.HOURS.toMillis(1)

    return runEveryAt(periodInMilliseconds, startDate, action)

}

inline fun runMinutelyAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createDateFromTimeOfDayAsString(timeOfDay)
    val periodInMilliseconds = TimeUnit.MINUTES.toMillis(1)

    return runEveryAt(periodInMilliseconds, startDate, action)
}

inline fun runOnceAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()
    val startDate = createDateFromTimeOfDayAsString(timeOfDay)

    val timer = Timer(handle, true)
    timer.schedule(startDate, action)

    return LifeCycleHandler(timer)
}

inline fun runOnceInSeconds(seconds: Int, crossinline callback: TimerTask.() -> Unit): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()

    val timer = Timer(handle, true)
    timer.schedule(seconds * 1000L, callback)

    return LifeCycleHandler(timer)
}

inline fun runOnceInMinutes(minutes: Int, crossinline action: TimerTask.() -> Unit) =
    runOnceInSeconds(minutes * 60, action)

inline fun runOnceInHours(hours: Int, crossinline action: TimerTask.() -> Unit) =
    runOnceInSeconds((hours * 60) * 60, action)

fun createDateFromTimeOfDayAsString(timeOfDay: String): Date {
    val (hour, minute) = timeOfDay.split(":")
    val startLocalDate = LocalDate.now().atTime(hour.toInt(), minute.toInt())
    return convertLocalDateTimeToDate(startLocalDate)
}

fun convertLocalDateTimeToDate(localDate: LocalDateTime): Date = Date
    .from(localDate.atZone(ZoneId.systemDefault()).toInstant())

class LifeCycleHandler(timer: Timer): LifeCycleHandlerInterface {
    override val lazyCancellation: Unit by lazy { timer.cancel() }

    override fun cancel() = lazyCancellation
    override fun cancelInSeconds(seconds: Int) = runOnceInSeconds(seconds) { lazyCancellation }
    override fun cancelInMinutes(minutes: Int) = runOnceInMinutes(minutes) { lazyCancellation }

}

interface LifeCycleHandlerInterface {
    val lazyCancellation: Unit
    fun cancel()
    fun cancelInSeconds(seconds: Int): LifeCycleHandlerInterface
    fun cancelInMinutes(minutes: Int): LifeCycleHandlerInterface
}