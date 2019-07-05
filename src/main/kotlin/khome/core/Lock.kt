package khome.core

import java.util.*

open class Lock<T> {

    private val list = Collections.synchronizedList(mutableListOf<T>())

    infix fun hasLocked(item: T): Boolean {
        return item in list
    }

    fun lock(item: T) {
        list += item
    }

    fun unLock(item: T) {
        list -= item
    }

    fun unLockAll() = list.clear()
}