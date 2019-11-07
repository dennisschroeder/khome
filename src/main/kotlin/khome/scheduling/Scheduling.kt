package khome.scheduling

import khome.Khome.Companion.isSandBoxModeActive
import khome.core.entities.Sun
import khome.core.entities.inputDateTime.AbstractTimeEntity
import khome.core.scheduledPeriodicTask
import khome.core.scheduledTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import java.lang.Thread.sleep
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Runs a [TimerTask] as daily routine starting at a specific date time
 *
 * @param localDateTime A local date time object representing the time the routine starts
 * @param action A TimerTask extension function
 *
 */
suspend fun runDailyAt(timeOfDay: String, action: suspend CoroutineScope.() -> Unit) {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    return runDailyAt(startDate, action)
}

/**
 * Runs a [TimerTask] as daily routine starting at a specific date time that gets extracted from an entity object
 *
 * @param entity An entity object that inherits from [AbstractTimeEntity] representing the time the routine starts
 * @param action A TimerTask extension function
 *
 */
suspend fun runDailyAt(entity: AbstractTimeEntity, action: suspend CoroutineScope.() -> Unit) {
    val dayTime = determineDayTimeFromTimeEntity(entity)
    return runDailyAt(dayTime, action)
}

/**
 * Runs a [TimerTask] as daily routine starting at a specific date time
 *
 * @param timeOfDay A string with the hour and minutes ("14:00") when to start the routine starts
 * @param action A TimerTask extension function
 *
 */
suspend fun runDailyAt(localDateTime: LocalDateTime, action: suspend CoroutineScope.() -> Unit) {

    val startDate = localDateTime
    val nextStartDate = startDate.plusDays(1)
    val nextExecution =
        if (nowIsAfter(localDateTime)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.DAYS.toMillis(1)
    return runEveryAt(nextExecution, periodInMilliseconds, action)
}

/**
 * Runs a [TimerTask] as hourly routine starting at a specific date time
 *
 * @param localDateTime A local date time object representing the time the routine starts
 * @param action A TimerTask extension function
 *
 */
suspend fun runHourlyAt(localDateTime: LocalDateTime, action: suspend CoroutineScope.() -> Unit) {
    val timeOfDay = determineTimeOfDayFromLocalDateTime(localDateTime)
    return runHourlyAt(timeOfDay, action)
}

/**
 * Runs a [TimerTask] as hourly routine starting at a specific date time
 *
 * @param entity An entity object that inherits from [AbstractTimeEntity] representing the time the routine starts
 * @param action A TimerTask extension function
 *
 */
suspend fun runHourlyAt(entity: AbstractTimeEntity, action: suspend CoroutineScope.() -> Unit) {
    val dayTime = determineDayTimeFromTimeEntity(entity)
    return runHourlyAt(dayTime, action)
}

/**
 * Runs a [TimerTask] as hourly routine starting at a specific date time
 *
 * @param timeOfDay A string with the hour and minutes ("14:00") when to start the routine starts
 * @param action A TimerTask extension function
 *
 */
suspend fun runHourlyAt(timeOfDay: String, action: suspend CoroutineScope.() -> Unit) {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    val now = LocalDateTime.now()
    val hoursSinceStartDate = startDate.until(now, ChronoUnit.HOURS) + 1
    val nextStartDate = startDate.plusHours(hoursSinceStartDate)
    val nextExecution =
        if (nowIsAfter(timeOfDay)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.HOURS.toMillis(1)

    return runEveryAt(nextExecution, periodInMilliseconds, action)
}

/**
 * Runs a [TimerTask] as minutely routine starting at a specific date time
 *
 * @param localDateTime A local date time object representing the time the routine starts
 * @param action A TimerTask extension function
 *
 */
suspend fun runMinutelyAt(localDateTime: LocalDateTime, action: suspend CoroutineScope.() -> Unit) {
    val timeOfDay = determineTimeOfDayFromLocalDateTime(localDateTime)
    return runMinutelyAt(timeOfDay, action)
}

/**
 * Runs a [TimerTask] as minutely routine starting at a specific date time
 *
 * @param entity An entity object that inherits from [AbstractTimeEntity] representing the time the routine starts
 * @param action A TimerTask extension function
 *
 */
suspend fun runMinutelyAt(entity: AbstractTimeEntity, action: suspend CoroutineScope.() -> Unit) {
    val dayTime = determineDayTimeFromTimeEntity(entity)
    return runMinutelyAt(dayTime, action)
}

/**
 * Runs a [TimerTask] as minutely routine starting at a specific date time
 *
 * @param timeOfDay A string with the hour and minutes ("14:00") when to start the routine starts
 * @param action A TimerTask extension function
 *
 */
suspend fun runMinutelyAt(timeOfDay: String, action: suspend CoroutineScope.() -> Unit) {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    val now = LocalDateTime.now()
    val minutesSinceStartDate = startDate.until(now, ChronoUnit.MINUTES) + 1
    val nextStartDate = startDate.plusMinutes(minutesSinceStartDate)
    val nextExecution =
        if (nowIsAfter(timeOfDay)) nextStartDate else startDate
    val periodInMilliseconds = TimeUnit.MINUTES.toMillis(1)

    return runEveryAt(nextExecution, periodInMilliseconds, action)
}

private fun determineTimeOfDayFromLocalDateTime(localDateTime: LocalDateTime) =
    "${localDateTime.hour}:${localDateTime.minute}"

private fun determineDayTimeFromTimeEntity(timeEntity: AbstractTimeEntity): String {
    val hour = timeEntity.time.hour
    val minute = timeEntity.time.minute
    return "$hour:$minute"
}

/**
 * Runs a [TimerTask] periodically for a given number of times
 *
 * @param period A period of time between execution of the [TimerTask] in milliseconds
 * @param executions The number of executions
 * @param action A TimerTask extension function.
 */
suspend fun runEveryTimePeriodFor(
    period: Long,
    executions: Int,
    action: suspend () -> Unit
) {
    var counter = 0
    return runEveryAt(LocalDateTime.now(), period) {
        action()
        counter++
        if (counter == executions) cancel()
    }
}

/**
 * Runs a [TimerTask] as periodically routine starting at a specific date time
 *
 * @param period A period of time between execution of the [TimerTask] in milliseconds
 * @param localDateTime A local date time object representing the time the routine starts
 * @param action A TimerTask extension function
 */
suspend fun runEveryAt(
    localDateTime: LocalDateTime,
    period: Long,
    action: suspend CoroutineScope.() -> Unit
): Unit = scheduledPeriodicTask(localDateTime, period, Dispatchers.Default, action)

/**
 * Runs a [TimerTask] once at a specific date time
 *
 * @param timeOfDay A string with the hour and minutes ("14:00") when to start the [TimerTask] gets executed.
 * @param action A TimerTask extension function
 * @return [Job]
 */
suspend fun <T> runOnceAt(timeOfDay: String, action: suspend CoroutineScope.() -> T): T {
    val startDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    return runOnceAt(startDate, action)
}

/**
 * Runs a [TimerTask] once at a specific date time.
 *
 * @param entity An entity object that inherits from [AbstractTimeEntity] representing the time the [TimerTask] gets executed.
 * @param action A TimerTask extension function.
 * @return [Job]
 */
suspend fun <T> runOnceAt(entity: AbstractTimeEntity, action: suspend CoroutineScope.() -> T): T {
    val dayTime = determineDayTimeFromTimeEntity(entity)
    return runOnceAt(dayTime, action)
}

/**
 * Runs a [TimerTask] once at a specific date time.
 *
 * @param localDateTime A local date time object representing the time the routine starts.
 * @param action A TimerTask extension function.
 * @return [Job]
 */
suspend fun <T> runOnceAt(
    localDateTime: LocalDateTime,
    action: suspend CoroutineScope.() -> T
): T = scheduledTask(localDateTime, Dispatchers.Default, action)

/**
 * Runs a [TimerTask] once in Hours after the function was called
 *
 * @param hours The time in minutes when the [TimerTask] will be executed once
 * @param action A TimerTask extension function.
 * @return [Job]
 */
suspend fun runOnceInHours(hours: Long, action: suspend CoroutineScope.() -> Unit) =
    runOnceInSeconds((hours * 60) * 60, action)

/**
 * Runs a [TimerTask] once in minutes after the function was called
 *
 * @param minutes The time in minutes when the [TimerTask] will be executed once
 * @param action A TimerTask extension function.
 * @return [Job]
 */
suspend fun <T> runOnceInMinutes(minutes: Long, action: suspend CoroutineScope.() -> T): T =
    runOnceInSeconds(minutes * 60, action)

/**
 * Runs a [TimerTask] once in Seconds after the function was called
 *
 * @param seconds The time in minutes when the [TimerTask] will be executed once
 * @param action A TimerTask extension function.
 * @return [Job]
 */
suspend fun <T> runOnceInSeconds(
    seconds: Long,
    action: suspend CoroutineScope.() -> T
): T = scheduledTask(seconds * 1000, Dispatchers.Default, action)

private fun createLocalDateTimeFromTimeOfDayAsString(timeOfDay: String): LocalDateTime {
    val (hour, minute) = timeOfDay.split(":")
    return LocalDate.now().atTime(hour.toInt(), minute.toInt())
}

/**
 * Runs a [TimerTask]  at sunrise.
 *
 * @param offsetInMinutes Define an offset for the execution time. Prefix an - for negative and + for positive offset.
 * @param action A TimerTask extension function.
 * @return [Job]
 */
suspend fun runEverySunRise(
    offsetInMinutes: String = "_",
    sun: Sun,
    action: suspend CoroutineScope.() -> Unit
) {
    val now = LocalDateTime.now()
    val dailyPeriodInMillis = TimeUnit.DAYS.toMillis(1)

    val offsetDirection = offsetInMinutes.first()
    val minutes = offsetInMinutes.substring(1)

    runEveryAt(now, dailyPeriodInMillis) {
        var nextSunrise = sun.nextSunrise
        when (offsetDirection) {
            '+' -> nextSunrise = nextSunrise.plusMinutes(minutes.toLong())
            '-' -> nextSunrise = nextSunrise.minusMinutes(minutes.toLong())
        }

        runOnceAt(nextSunrise, action)
    }
}

/**
 * Runs a [TimerTask]  at sunset.
 *
 * @param offsetInMinutes Define an offset for the execution time. Prefix an - for negative and + for positive offset
 * @param action A TimerTask extension function.
 * @return [Job]
 */
suspend fun runEverySunSet(
    offsetInMinutes: String = "_",
    sun: Sun,
    action: suspend CoroutineScope.() -> Unit
) {
    val now = LocalDateTime.now()
    val dailyPeriodInMillis = TimeUnit.DAYS.toMillis(1)

    val offsetDirection = offsetInMinutes.first()
    val minutes = offsetInMinutes.substring(1)

    runEveryAt(now, dailyPeriodInMillis) {
        var nextSunset = sun.nextSunset
        when (offsetDirection) {
            '+' -> nextSunset = nextSunset.plusMinutes(minutes.toLong())
            '-' -> nextSunset = nextSunset.minusMinutes(minutes.toLong())
        }
        runOnceAt(nextSunset, action)
    }
}

internal fun LocalDateTime.toDate(): Date = Date
    .from(atZone(ZoneId.systemDefault()).toInstant())

/**
 * Check if now (the time the function is executed) is after a given date time.
 *
 * @param timeOfDay A string with the hour and minutes ("14:00") when to start the [TimerTask] gets executed.
 */
fun nowIsAfter(timeOfDay: String): Boolean {
    val timeOfDayDate = createLocalDateTimeFromTimeOfDayAsString(timeOfDay)
    return nowIsAfter(timeOfDayDate)
}

/**
 * Check if now (the time the function is executed) is after a given date time.
 *
 * @param localDateTime The value to check against.
 */
fun nowIsAfter(localDateTime: LocalDateTime): Boolean {
    val now = LocalDateTime.now().toDate()
    return now.after(localDateTime.toDate())
}

/**
 * Delays the execution of an action. Is actually a wrapper around Kotlin's [sleep] function,
 * but does not delay running in sandbox mode.
 *
 * @param T The type of the return value of the action parameter, which is also the return type of this function.
 * @param millis The delay time in milliseconds
 * @param action An lambda function that gets executed after the delay
 *
 */
fun <T> runAfterDelay(millis: Long, action: () -> T): T {
    if (!isSandBoxModeActive)
        sleep(millis)
    return action()
}
