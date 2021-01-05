package com.jonnyzzz.mplay.agent.runtime

/**
 * The basic interface to send and intercept parameter (or return type)
 * values
 */
interface MPlayValuesVisitor {
    /**
     * Each of the `visit*` methods is used from the generated
     * bytecode to send and update every method arguments.
     * The default implementation returns the same value back,
     * but there are scenarios where it would make sense to
     * return a different value for a callback parameters
     */
    fun visitBoolean(v: Boolean): Boolean = v
    fun visitChar(v: Char): Char = v
    fun visitByte(v: Byte): Byte = v
    fun visitShort(v: Short): Short = v
    fun visitInt(v: Int): Int = v
    fun visitLong(v: Long): Long = v
    fun visitFloat(v: Float): Float = v
    fun visitDouble(v: Double): Double = v
    fun visitObject(v: Any?): Any? = v
}

/**
 * Similar to [MPlayValuesVisitor], this interface allows
 * to send and intercept an object that is about to be thrown
 * @see MPlayValuesVisitor
 */
interface MPlayExceptionVisitor {
    fun visitException(v: Throwable): Throwable = v
}
