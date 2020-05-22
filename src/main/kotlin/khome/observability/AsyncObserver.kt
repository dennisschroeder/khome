package khome.observability

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

class AsyncObserver<S>(
    private val f: suspend CoroutineScope.(snapshot: WithHistory<S>) -> Unit,
    context: CoroutineContext = Dispatchers.IO + DefaultEntityObserverExceptionHandler()
) : Observer<S>, CoroutineScope by CoroutineScope(context) {
    override var enabled: AtomicBoolean = AtomicBoolean(true)
    override fun update(state: WithHistory<S>) {
        if (!enabled.get()) return
        launch { f.invoke(this, state) }
    }
}
