package khome.extending.observer

import khome.KhomeApplication
import khome.extending.SunAttributes
import khome.extending.SunSnapshot
import khome.extending.SunState
import khome.observability.SwitchableObserver
import kotlinx.coroutines.CoroutineScope

@Suppress("FunctionName")
fun KhomeApplication.SunObserver(f: (snapshot: SunSnapshot, SwitchableObserver<SunState, SunAttributes>) -> Unit): SwitchableObserver<SunState, SunAttributes> =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.SunAsyncObserver(f: suspend CoroutineScope.(snapshot: SunSnapshot, SwitchableObserver<SunState, SunAttributes>) -> Unit): SwitchableObserver<SunState, SunAttributes> =
    AsyncObserver(f)
