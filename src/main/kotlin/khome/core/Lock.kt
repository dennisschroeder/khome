package khome.core

class Lock<T> {
    private val list = mutableListOf<T>()

    infix fun hasLocked(item: T): Boolean {
        return item in list
    }

    @Synchronized
    infix fun lock(item: T) {
        list += item
    }

    @Synchronized
    infix fun unLock(item: T) {
        list -= item
    }
}