package khome.extending.observer

import khome.KhomeApplication
import khome.extending.PositionableCoverSnapshot
import khome.observability.Switchable
import kotlinx.coroutines.CoroutineScope

@Suppress("FunctionName")
fun KhomeApplication.PositionableCoverObserver(f: (PositionableCoverSnapshot, Switchable) -> Unit): Switchable =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.PositionableCoverAsyncObserver(f: suspend CoroutineScope.(PositionableCoverSnapshot, Switchable) -> Unit): Switchable =
    AsyncObserver(f)
