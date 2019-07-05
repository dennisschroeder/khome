package khome.scheduling

import java.util.*
import java.time.ZoneId
import java.time.LocalDate
import kotlin.concurrent.*
import java.lang.Thread.sleep
import java.time.LocalDateTime
import khome.core.entities.Sun
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import khome.core.LifeCycleHandlerInterface
import khome.Khome.Companion.isSandBoxModeActive
import khome.Khome.Companion.subscribeSchedulerTestEvent
import khome.core.entities.inputDateTime.AbstractTimeEntity
import khome.Khome.Companion.subscribeSchedulerCancelEvents


/**
 * Runs a [TimerTask] as daily routine starting at a specific date time
 *
 * @param localDateTime A local date time object representing the time the routine starts
 * @param action A TimerTask extension function
 * @return [LifeCycleHandler]
 *
 */
fun runDailyAt(localDateTime: LocalDateTime, action: TimerTask.() -> Unit): LifeCycleHandler {
    val timeOfDay = determineTimeOfDayFromLocalDateTime(localDateTime)
    return runDailyAt(timeOfDay, action)
}

/**
 * Runs a [TimerTask] as daily routine starting at a specific date time that gets extracted from an entity object
 *
 * @param entity An entity object that inherits from [AbstractTimeEntity] representing the time the routine starts
 * @param action A TimerTask extension function
 * @return [LifeCycleHandler]
 *
 */
fun runDailyAt(entity: AbstractTimeEntity, action: TimerTask.() -> Unit): LifeCycleHandler {
    val dayTime = determineDayTimeFromTimeEntity(entity)
    return runDailyAt(dayTime, action)
}

/**
 * Runs a [TimerTask] as daily routine starting at a specific date time
 *
 * @param timeOfDay A string with the hour and minutes ("14:00") when to start the routine starts
 * @param action A TimerTask extension function
 * @return [LifeCycleHandler]
 *
 */
fun runDailyAt(timeOfDay: String, action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    val nextStartDate = startDate.plusDays(1)
    val nextExecution =
        if (nowIsAfter(timeOfDay)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.DAYS.toMillis(1)
    return runEveryAt(periodInMilliseconds, nextExecution, action)
}

/**
 * Run a [TimerTask] as hourly routine starting at a specific date time
 *
 * @param localDateTime A local date time object representing the time the routine starts
 * @param action A TimerTask extension function
 * @return [LifeCycleHandler]
 *
 */
fun runHourlyAt(localDateTime: LocalDateTime, action: TimerTask.() -> Unit): LifeCycleHandler {
    val timeOfDay = determineTimeOfDayFromLocalDateTime(localDateTime)
    return runHourlyAt(timeOfDay, action)
}

/**
 * Run a [TimerTask] as hourly routine starting at a specific date time
 *
 * @param entity An entity object that inherits from [AbstractTimeEntity] representing the time the routine starts
 * @param action A TimerTask extension function
 * @return [LifeCycleHandler]
 *
 */
fun runHourlyAt(entity: AbstractTimeEntity, action: TimerTask.() -> Unit): LifeCycleHandler {
    val dayTime = determineDayTimeFromTimeEntity(entity)
    return runHourlyAt(dayTime, action)
}

/**
 * Run a [TimerTask] as hourly routine starting at a specific date time
 *
 * @param timeOfDay A string with the hour and minutes ("14:00") when to start the routine starts
 * @param action A TimerTask extension function
 * @return [LifeCycleHandler]
 *
 */
fun runHourlyAt(timeOfDay: String, action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    val now = LocalDateTime.now()
    val hoursSinceStartDate = startDate.until(now, ChronoUnit.YEARS) + 1
    val nextStartDate = startDate.plusHours(hoursSinceStartDate)
    val nextExecution =
        if (nowIsAfter(timeOfDay)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.HOURS.toMillis(1)

    return runEveryAt(periodInMilliseconds, nextExecution, action)
}

/**
 * Run a [TimerTask] as minutely routine starting at a specific date time
 *
 * @param localDateTime A local date time object representing the time the routine starts
 * @param action A TimerTask extension function
 * @return [LifeCycleHandler]
 *
 */
fun runMinutelyAt(localDateTime: LocalDateTime, action: TimerTask.() -> Unit): LifeCycleHandler {
    val timeOfDay = determineTimeOfDayFromLocalDateTime(localDateTime)
    return runMinutelyAt(timeOfDay, action)
}

/**
 * Run a [TimerTask] as minutely routine starting at a specific date time
 *
 * @param entity An entity object that inherits from [AbstractTimeEntity] representing the time the routine starts
 * @param action A TimerTask extension function
 * @return [LifeCycleHandler]
 *
 */
fun runMinutelyAt(entity: AbstractTimeEntity, action: TimerTask.() -> Unit): LifeCycleHandler {
    val dayTime = determineDayTimeFromTimeEntity(entity)
    return runMinutelyAt(dayTime, action)
}

/**
 * Run a [TimerTask] as minutely routine starting at a specific date time
 *
 * @param timeOfDay A string with the hour and minutes ("14:00") when to start the routine starts
 * @param action A TimerTask extension function
 * @return [LifeCycleHandler]
 *
 */
fun runMinutelyAt(timeOfDay: String, action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    val now = LocalDateTime.now()
    val minutesSinceStartDate = startDate.until(now, ChronoUnit.MINUTES) + 1
    val nextStartDate = startDate.plusMinutes(minutesSinceStartDate)
    val nextExecution =
        if (nowIsAfter(timeOfDay)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.MINUTES.toMillis(1)

    return runEveryAt(periodInMilliseconds, nextExecution, action)
}

private fun determineTimeOfDayFromLocalDateTime(localDateTime: LocalDateTime) =
    "${localDateTime.hour}:${localDateTime.minute}"

private fun determineDayTimeFromTimeEntity(timeEntity: AbstractTimeEntity): String {
    val hour = timeEntity.time.hour
    val minute = timeEntity.time.minute
    return "$hour:$minute"
}

/**
 * Run a [TimerTask] as periodically routine starting at a specific date time
 *
 * @param period A period of time between execution of the [TimerTask] in milliseconds
 * @param localDateTime A local date time object representing the time the routine starts
 * @param action A TimerTask extension function
 * @return [LifeCycleHandler]
 *
 */
fun runEveryAt(
    period: Long,
    localDateTime: LocalDateTime,
    action: TimerTask.() -> Unit
): LifeCycleHandler {

    val timerTask = timerTask(action)
    subscribeSchedulerTestEvent { action(timerTask) }

    val timer = Timer("scheduler", false)
    if (!isSandBoxModeActive)
        timer.scheduleAtFixedRate(timerTask, localDateTime.toDate(), period)
    val lifeCycleHandler = LifeCycleHandler(timer)
    subscribeSchedulerCancelEvents { lifeCycleHandler.cancel() }

    return lifeCycleHandler
}

/**
 * Run a [TimerTask] once at a specific date time
 *
 * @param timeOfDay A string with the hour and minutes ("14:00") when to start the [TimerTask] gets executed.
 * @param action A TimerTask extension function
 * @return [LifeCycleHandler]
 */
fun runOnceAt(timeOfDay: String, action: TimerTask.() -> Unit): LifeCycleHandler {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    return runOnceAt(startDate, action)
}

/**
 * Run a [TimerTask] once at a specific date time.
 *
 * @param entity An entity object that inherits from [AbstractTimeEntity] representing the time the [TimerTask] gets executed.
 * @param action A TimerTask extension function.
 * @return [LifeCycleHandler]
 */
fun runOnceAt(entity: AbstractTimeEntity, action: TimerTask.() -> Unit): LifeCycleHandler {
    val dayTime = determineDayTimeFromTimeEntity(entity)
    return runOnceAt(dayTime, action)
}

/**
 * Run a [TimerTask] once at a specific date time.
 *
 * @param localDateTime A local date time object representing the time the routine starts.
 * @param action A TimerTask extension function.
 * @return [LifeCycleHandler]
 */
fun runOnceAt(localDateTime: LocalDateTime, action: TimerTask.() -> Unit): LifeCycleHandler {
    val timerTask = timerTask(action)
    subscribeSchedulerTestEvent { action(timerTask) }

    val timer = Timer("scheduler", false)
    if (!isSandBoxModeActive)
        timer.schedule(timerTask, localDateTime.toDate())

    val lifeCycleHandler = LifeCycleHandler(timer)
    subscribeSchedulerCancelEvents { lifeCycleHandler.cancel() }

    return lifeCycleHandler
}

inline fun runEveryTimePeriodFor(
    timePeriod: Long,
    executions: Int,
    crossinline task: () -> Unit
): LifeCycleHandler {
    var counter = 0
    val timer = runEveryAt(timePeriod, LocalDateTime.now()) {
        task()
        counter++
        if (counter == executions) cancel()
    }

    return timer
}

fun runOnceInMinutes(minutes: Int, action: TimerTask.() -> Unit) =
    runOnceInSeconds(minutes * 60, action)

fun runOnceInHours(hours: Int, action: TimerTask.() -> Unit) =
    runOnceInSeconds((hours * 60) * 60, action)

fun runOnceInSeconds(seconds: Int, action: TimerTask.() -> Unit): LifeCycleHandler {
    val timerTask = timerTask(action)
    subscribeSchedulerTestEvent { action(timerTask) }

    val timer = Timer("scheduler", false)
    if (!isSandBoxModeActive)
        timer.schedule(timerTask, seconds * 1000L)

    val lifeCycleHandler = LifeCycleHandler(timer)
    subscribeSchedulerCancelEvents { lifeCycleHandler.cancel() }

    return lifeCycleHandler
}

private fun createLocalDateTimeFromTimeOfDayAsString(timeOfDay: String): LocalDateTime {
    val (hour, minute) = timeOfDay.split(":")
    return LocalDate.now().atTime(hour.toInt(), minute.toInt())
}

fun runEverySunRise(offsetInMinutes: String = "_", action: TimerTask.() -> Unit) {
    val now = LocalDateTime.now()
    val dailyPeriodInMillis = TimeUnit.DAYS.toMillis(1)

    val offsetDirection = offsetInMinutes.first()
    val minutes = offsetInMinutes.substring(1)

    runEveryAt(dailyPeriodInMillis, now) {
        var nextSunrise = Sun.nextSunrise
        when (offsetDirection) {
            '+' -> nextSunrise = nextSunrise.plusMinutes(minutes.toLong())
            '-' -> nextSunrise = nextSunrise.minusMinutes(minutes.toLong())
        }
        runOnceAt(nextSunrise, action)
    }
}

fun runEverySunSet(offsetInMinutes: String = "_", action: TimerTask.() -> Unit) {
    val now = LocalDateTime.now()
    val dailyPeriodInMillis = TimeUnit.DAYS.toMillis(1)

    val offsetDirection = offsetInMinutes.first()
    val minutes = offsetInMinutes.substring(1)

    runEveryAt(dailyPeriodInMillis, now) {
        var nextSunset = Sun.nextSunset
        when (offsetDirection) {
            '+' -> nextSunset = nextSunset.plusMinutes(minutes.toLong())
            '-' -> nextSunset = nextSunset.minusMinutes(minutes.toLong())
        }
        runOnceAt(nextSunset, action)
    }
}

class LifeCycleHandler(private val timer: Timer) : LifeCycleHandlerInterface {
    override val lazyCancellation by lazy {
        timer.cancel()
    }

    fun cancel() = lazyCancellation
    fun cancelInSeconds(seconds: Int) = runOnceInSeconds(seconds) { lazyCancellation }
    fun cancelInMinutes(minutes: Int) = runOnceInMinutes(minutes) { lazyCancellation }
}

internal fun LocalDateTime.toDate(): Date = Date
    .from(atZone(ZoneId.systemDefault()).toInstant())

fun nowIsAfter(timeOfDay: String): Boolean {
    val timeOfDayDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    return nowIsAfter(timeOfDayDate)
}

fun nowIsAfter(localDateTime: LocalDateTime): Boolean {
    val now = LocalDateTime.now().toDate()
    return now.after(localDateTime.toDate())
}

/**
 * Delays the execution of an action. Is actually a wrapper around Kotlin's [sleep] function,
 * but does not delay running in sandbox mode.
 *
 * @param millis The delay time in milliseconds
 * @param action An lambda function that gets executed after the delay
 *
 */
fun <T> runAfterDelay(millis: Long, action: () -> T): T {
    if (!isSandBoxModeActive)
        sleep(millis)
    return action()
}
