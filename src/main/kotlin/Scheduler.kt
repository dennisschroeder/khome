package khome
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.*

inline fun runOnceAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit) {
    val handle = UUID.randomUUID().toString()
    val startDate = createDateFromTimeOfDayAsString(timeOfDay)

    val timer = Timer(handle, true)
    timer.schedule(startDate, action)
}

inline fun runDailyAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit) {
    val handle = UUID.randomUUID().toString()
    val startDate = createDateFromTimeOfDayAsString(timeOfDay)
    val periodInMilliseconds = TimeUnit.DAYS.toMillis(1)

    fixedRateTimer(handle, true, startDate, periodInMilliseconds,  action)
}

inline fun runInSeconds(seconds: Int, crossinline callback: TimerTask.() -> Unit) {
    val handle = UUID.randomUUID().toString()

    val timer = Timer(handle, true)
    timer.schedule(seconds * 1000L, callback)
}

inline fun runInMinutes(minutes: Int, crossinline action: TimerTask.() -> Unit) = runInSeconds(minutes * 60, action)

inline fun runInHours(hours: Int, crossinline action: TimerTask.() -> Unit) = runInSeconds((hours * 60) * 60, action)

fun createDateFromTimeOfDayAsString(timeOfDay: String): Date {
    val (hour, minute) = timeOfDay.split(":")
    val startLocalDate = LocalDate.now().atTime(hour.toInt(),minute.toInt())
    return convertLocalDateTimeToDate(startLocalDate)
}

fun convertLocalDateTimeToDate(localDate: LocalDateTime) = Date
    .from(localDate.atZone(ZoneId.systemDefault()).toInstant())