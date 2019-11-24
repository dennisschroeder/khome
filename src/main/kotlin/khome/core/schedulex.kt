package khome.core

import java.time.LocalDateTime
import kotlinx.coroutines.delay
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

suspend fun periodicTask(
    period: Long,
    context: CoroutineContext = EmptyCoroutineContext,
    task: suspend CoroutineScope.() -> Unit
) = withContext(context) {
    while (true) {
        delay(period)
        task()
    }
}

suspend fun <T> scheduledTask(
    time: LocalDateTime,
    context: CoroutineContext = EmptyCoroutineContext,
    task: suspend CoroutineScope.() -> T
): T {
    val now = LocalDateTime.now()
    val timeDelta = calcTimeDeltaInMillis(now, time)
    return scheduledTask(time = timeDelta, context = context, task = task)
}

suspend fun <T> scheduledTask(
    time: Long,
    context: CoroutineContext = EmptyCoroutineContext,
    task: suspend CoroutineScope.() -> T
): T =
    withContext(context) {
        delay(time)
        task()
    }

suspend fun scheduledPeriodicTask(
    time: LocalDateTime,
    period: Long,
    context: CoroutineContext = EmptyCoroutineContext,
    task: suspend CoroutineScope.() -> Unit
) {
    val now = LocalDateTime.now()
    val timeDelta = calcTimeDeltaInMillis(now, time)
    scheduledPeriodicTask(time = timeDelta, period = period, context = context, task = task)
}

suspend fun scheduledPeriodicTask(
    time: Long,
    period: Long,
    context: CoroutineContext = EmptyCoroutineContext,
    task: suspend CoroutineScope.() -> Unit
): Unit =
    withContext(context) {
        delay(time)
        while (true) {
            task()
            delay(period)
        }
    }

fun calcTimeDeltaInMillis(timeA: LocalDateTime, timeB: LocalDateTime): Long {
    return timeA.until(timeB, ChronoUnit.MILLIS)
}
