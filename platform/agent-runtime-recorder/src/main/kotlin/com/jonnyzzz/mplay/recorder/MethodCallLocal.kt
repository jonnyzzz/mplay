package com.jonnyzzz.mplay.recorder

import java.util.concurrent.atomic.AtomicLong

object MethodCallLocal {
    private val callIds = AtomicLong()
    private val pendingMethods = ThreadLocal<Long?>()

    fun tryRegisterNextCall() : Long? {
        if (pendingMethods.get() != null) return null
        val nextId = callIds.incrementAndGet()
        pendingMethods.set(nextId)
        return tryRegisterNextCall()
    }

    fun onCallCompleted(callId: Long) {
        if (pendingMethods.get() == callId) {
            pendingMethods.remove()
        }
    }
}