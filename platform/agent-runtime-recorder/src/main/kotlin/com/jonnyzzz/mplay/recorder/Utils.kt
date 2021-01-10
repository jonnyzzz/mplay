package com.jonnyzzz.mplay.recorder

import java.util.*

class ObjectsEnumerator<T : Any> {
    private val cache = IdentityHashMap<T, Int>()

    fun enumerate(t: T): Int {
        synchronized(this) {
            return cache.computeIfAbsent(t) {
                cache.size + 1
            }
        }
    }
}
