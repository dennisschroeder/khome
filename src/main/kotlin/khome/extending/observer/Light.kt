package khome.extending.observer

import khome.KhomeApplication
import khome.extending.DimmableLightSnapshot
import khome.extending.SwitchableLightSnapshot
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope

@Suppress("FunctionName")
fun KhomeApplication.SwitchableLightObserver(f: (SwitchableLightSnapshot, Switchable) -> Unit): Switchable =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.SwitchableAsyncObserver(f: suspend CoroutineScope.(SwitchableLightSnapshot, Switchable) -> Unit): Switchable =
    AsyncObserver(f)

@Suppress("FunctionName")
fun KhomeApplication.DimmableLightObserver(f: (DimmableLightSnapshot, Switchable) -> Unit): Switchable =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.DimmableLightAsyncObserver(f: suspend CoroutineScope.(DimmableLightSnapshot, Switchable) -> Unit): Switchable =
    AsyncObserver(f)
