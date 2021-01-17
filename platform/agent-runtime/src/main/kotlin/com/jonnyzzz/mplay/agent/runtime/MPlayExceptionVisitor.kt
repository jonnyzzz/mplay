package com.jonnyzzz.mplay.agent.runtime

/**
 * Similar to [MPlayValuesVisitor], this interface allows
 * to send and intercept an object that is about to be thrown
 *
 * @see MPlayValuesVisitor
 */
interface MPlayExceptionVisitor {
    fun visitException(v: Throwable): Throwable = v
}
