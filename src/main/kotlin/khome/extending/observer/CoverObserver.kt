package khome.extending.observer

import khome.KhomeApplication
import khome.extending.CoverState
import khome.extending.PositionableCoverAttributes
import khome.extending.PositionableCoverSnapshot
import khome.observability.SwitchableObserver
import kotlinx.coroutines.CoroutineScope

@Suppress("FunctionName")
fun KhomeApplication.PositionableCoverObserver(f: (PositionableCoverSnapshot, SwitchableObserver<CoverState, PositionableCoverAttributes>) -> Unit): SwitchableObserver<CoverState, PositionableCoverAttributes> =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.PositionableCoverAsyncObserver(f: suspend CoroutineScope.(PositionableCoverSnapshot, SwitchableObserver<CoverState, PositionableCoverAttributes>) -> Unit): SwitchableObserver<CoverState, PositionableCoverAttributes> =
    AsyncObserver(f)
