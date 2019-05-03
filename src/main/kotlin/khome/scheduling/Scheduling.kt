package khome.scheduling

import java.util.*
import java.time.ZoneId
import khome.core.logger
import java.time.LocalDate
import kotlin.concurrent.*
import java.time.LocalDateTime
import khome.listening.getState
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import java.time.format.DateTimeFormatter
import khome.core.LifeCycleHandlerInterface

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

inline fun runOnceAt(date: Date, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val handle = UUID.randomUUID().toString()

    val timer = Timer(handle, true)
    timer.schedule(date, action)

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
    return startLocalDate.toDate()
}

fun runEverySunRise(action: TimerTask.() -> Unit) {
    val now = LocalDateTime.now().toDate()
    val dailyPeriodInMillis = TimeUnit.DAYS.toMillis(1)

    runEveryAt(dailyPeriodInMillis, now) {
        val nextSunrise = nextSunrise()
        runOnceAt(nextSunrise, action)
    }
}

fun runEverySunSet(action: TimerTask.() -> Unit) {
    val now = LocalDateTime.now().toDate()
    val dailyPeriodInMillis = TimeUnit.DAYS.toMillis(1)

    runEveryAt(dailyPeriodInMillis, now) {
        val nextSunrise = nextSunset()
        runOnceAt(nextSunrise, action)
    }
}

fun nextSunrise(): Date = getNextSunPosition("next_rising")

fun nextSunset(): Date = getNextSunPosition("next_setting")

fun isSunUp() = getState("sun.sun")!!.get<String>() == "above_horizon"

fun isSunDown() = getState("sun.sun")!!.get<String>() == "below_horizon"

@Throws(RuntimeException::class)
fun getNextSunPosition(nextPosition: String): Date {
    val sunset = getState("sun.sun")?.getAttribute<String>(nextPosition)
        ?: throw RuntimeException("Could not fetch $nextPosition time from state-attribute")
    val sunsetLocaleDateTime = LocalDateTime.parse(sunset, DateTimeFormatter.ISO_DATE_TIME)

    return sunsetLocaleDateTime.toDate()
}

class LifeCycleHandler(timer: Timer) : LifeCycleHandlerInterface {
    override val lazyCancellation: Unit by lazy {
        timer.cancel()
        logger.info { "Schedule canceled." }
    }

    override fun cancel() = lazyCancellation
    override fun cancelInSeconds(seconds: Int) = runOnceInSeconds(seconds) { lazyCancellation }
    override fun cancelInMinutes(minutes: Int) = runOnceInMinutes(minutes) { lazyCancellation }
}

fun LocalDateTime.toDate(): Date = Date
    .from(atZone(ZoneId.systemDefault()).toInstant())