package khome.core.mapping

interface KhomeTypeAdapter<T> {
    fun <P> from(value: P): T
    fun <P> to(value: T): P
}
