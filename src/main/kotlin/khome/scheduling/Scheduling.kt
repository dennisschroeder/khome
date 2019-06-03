package khome.scheduling

import khome.Khome.Companion.isSandBoxModeActive
import java.util.*
import java.time.ZoneId
import java.time.LocalDate
import kotlin.concurrent.*
import khome.core.entities.Sun
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import khome.listening.getEntityInstance
import khome.core.LifeCycleHandlerInterface
import khome.core.entities.inputDateTime.AbstractTimeEntity

inline fun <reified Entity : AbstractTimeEntity> runDailyAt(crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val entity = getEntityInstance<Entity>()
    val dayTime = determineDayTimeFromTimeEntity(entity)

    return runDailyAt(dayTime, action)
}

inline fun runDailyAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    val nextStartDate = startDate.plusDays(1)
    val nextExecution =
        if (nowIsAfter(timeOfDay)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.DAYS.toMillis(1)
    return runEveryAt(periodInMilliseconds, nextExecution, action)
}

inline fun <reified Entity : AbstractTimeEntity> runHourlyAt(crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val entity = getEntityInstance<Entity>()
    val dayTime = determineDayTimeFromTimeEntity(entity)

    return runHourlyAt(dayTime, action)
}

inline fun runHourlyAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    val now = LocalDateTime.now()
    val hoursSinceStartDate = startDate.until(now, ChronoUnit.YEARS) + 1
    val nextStartDate = startDate.plusHours(hoursSinceStartDate)
    val nextExecution =
        if (nowIsAfter(timeOfDay)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.HOURS.toMillis(1)

    return runEveryAt(periodInMilliseconds, nextExecution, action)
}

inline fun <reified Entity : AbstractTimeEntity> runMinutelyAt(crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val entity = getEntityInstance<Entity>()
    val dayTime = determineDayTimeFromTimeEntity(entity)

    return runMinutelyAt(dayTime, action)
}

inline fun runMinutelyAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    val now = LocalDateTime.now()
    val minutesSinceStartDate = startDate.until(now, ChronoUnit.MINUTES) + 1
    val nextStartDate = startDate.plusMinutes(minutesSinceStartDate)
    val nextExecution =
        if (nowIsAfter(timeOfDay)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.MINUTES.toMillis(1)

    return runEveryAt(periodInMilliseconds, nextExecution, action)
}

fun determineDayTimeFromTimeEntity(timeEntity: AbstractTimeEntity): String {
    val hour = timeEntity.time.hour
    val minute = timeEntity.time.minute
    return "$hour:$minute"
}

inline fun runEveryAt(
    period: Long,
    localDateTime: LocalDateTime,
    crossinline action: TimerTask.() -> Unit
): LifeCycleHandler {

    val timer = when(isSandBoxModeActive()) {
        true -> fixedRateTimer("scheduler", true, LocalDateTime.now().toDate(), period, action)
        false -> fixedRateTimer("scheduler", true, localDateTime.toDate(), period, action)
    }

    return LifeCycleHandler(timer)
}

inline fun runOnceAt(timeOfDay: String, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    return runOnceAt(startDate, action)
}

inline fun runOnceAt(dateTime: LocalDateTime, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val timer = Timer("scheduler", false)
    when(isSandBoxModeActive()) {
        true ->  timer.schedule(LocalDateTime.now().toDate(), action)
        false ->  timer.schedule(dateTime.toDate(), action)
    }

    return LifeCycleHandler(timer)
}

inline fun runOnceInSeconds(seconds: Int, crossinline action: TimerTask.() -> Unit): LifeCycleHandler {
    val timer = Timer("scheduler", false)
    when(isSandBoxModeActive()) {
        true ->  timer.schedule(LocalDateTime.now().toDate(), action)
        false ->  timer.schedule(seconds * 1000L, action)
    }
    return LifeCycleHandler(timer)
}

inline fun runOnceInMinutes(minutes: Int, crossinline action: TimerTask.() -> Unit) =
    runOnceInSeconds(minutes * 60, action)

inline fun runOnceInHours(hours: Int, crossinline action: TimerTask.() -> Unit) =
    runOnceInSeconds((hours * 60) * 60, action)

fun createLocalDateTimeFromTimeOfDayAsString(timeOfDay: String): LocalDateTime {
    val (hour, minute) = timeOfDay.split(":")
    return LocalDate.now().atTime(hour.toInt(), minute.toInt())
}

fun runEverySunRise(action: TimerTask.() -> Unit) {
    val now = LocalDateTime.now()
    val dailyPeriodInMillis = TimeUnit.DAYS.toMillis(1)

    runEveryAt(dailyPeriodInMillis, now) {
        val nextSunrise = nextSunrise()
        runOnceAt(nextSunrise, action)
    }
}

fun runEverySunSet(action: TimerTask.() -> Unit) {
    val now = LocalDateTime.now()
    val dailyPeriodInMillis = TimeUnit.DAYS.toMillis(1)

    runEveryAt(dailyPeriodInMillis, now) {
        val nextSunset = nextSunset()
        runOnceAt(nextSunset, action)
    }
}

fun nextSunrise() = getNextSunPosition("next_rising")

fun nextSunset() = getNextSunPosition("next_setting")


fun getNextSunPosition(nextPosition: String): LocalDateTime {
    val nextSunPositionChange = Sun.getAttributeValue<String>(nextPosition)
    return convertUtcToLocalDateTime(nextSunPositionChange)
}

fun convertUtcToLocalDateTime(utcDateTime: String): LocalDateTime {
    val offsetDateTime = OffsetDateTime.parse(utcDateTime)
    val zonedDateTime = offsetDateTime.atZoneSameInstant(ZoneId.systemDefault())
    return zonedDateTime.toLocalDateTime()
}

class LifeCycleHandler(private val timer: Timer) : LifeCycleHandlerInterface {
    override val lazyCancellation by lazy {
        timer.cancel()
    }

    fun cancel() = lazyCancellation
    fun cancelInSeconds(seconds: Int) = runOnceInSeconds(seconds) { lazyCancellation }
    fun cancelInMinutes(minutes: Int) = runOnceInMinutes(minutes) { lazyCancellation }
}

fun LocalDateTime.toDate(): Date = Date
    .from(atZone(ZoneId.systemDefault()).toInstant())

fun nowIsAfter(timeOfDay: String): Boolean {
    val timeOfDayDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    return nowIsAfter(timeOfDayDate)
}

fun nowIsAfter(localDateTime: LocalDateTime): Boolean {
    val now = LocalDateTime.now().toDate()
    return now.after(localDateTime.toDate())
}