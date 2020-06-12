package khome.extending.observer

import khome.KhomeApplication
import khome.extending.SunSnapshot
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope

@Suppress("FunctionName")
fun KhomeApplication.SunObserver(f: (snapshot: SunSnapshot, Switchable) -> Unit): Switchable =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.SunAsyncObserver(f: suspend CoroutineScope.(snapshot: SunSnapshot, Switchable) -> Unit): Switchable =
    AsyncObserver(f)
