package khome.extending.observer

import khome.KhomeApplication
import khome.extending.DimmableLightAttributes
import khome.extending.DimmableLightSnapshot
import khome.extending.DimmableLightState
import khome.extending.LightAttributes
import khome.extending.SwitchableLightSnapshot
import khome.extending.SwitchableState
import khome.observability.SwitchableObserver
import kotlinx.coroutines.CoroutineScope

@Suppress("FunctionName")
fun KhomeApplication.SwitchableLightObserver(f: (SwitchableLightSnapshot, SwitchableObserver<SwitchableState, LightAttributes>) -> Unit): SwitchableObserver<SwitchableState, LightAttributes> =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.SwitchableAsyncObserver(f: suspend CoroutineScope.(SwitchableLightSnapshot, SwitchableObserver<SwitchableState, LightAttributes>) -> Unit): SwitchableObserver<SwitchableState, LightAttributes> =
    AsyncObserver(f)

@Suppress("FunctionName")
fun KhomeApplication.DimmableLightObserver(f: (DimmableLightSnapshot, SwitchableObserver<DimmableLightState, DimmableLightAttributes>) -> Unit): SwitchableObserver<DimmableLightState, DimmableLightAttributes> =
    Observer(f)

@Suppress("FunctionName")
fun KhomeApplication.DimmableLightAsyncObserver(f: suspend CoroutineScope.(DimmableLightSnapshot, SwitchableObserver<DimmableLightState, DimmableLightAttributes>) -> Unit): SwitchableObserver<DimmableLightState, DimmableLightAttributes> =
    AsyncObserver(f)
