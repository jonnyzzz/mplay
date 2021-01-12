package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.MPlayValuesVisitor

open class ParametersToListVisitor : MPlayValuesVisitor {
    private val params = mutableListOf<Any?>()

    override fun visitBoolean(v: Boolean): Boolean {
        params += v
        return super.visitBoolean(v)
    }

    override fun visitChar(v: Char): Char {
        params += v
        return super.visitChar(v)
    }

    override fun visitByte(v: Byte): Byte {
        params += v
        return super.visitByte(v)
    }

    override fun visitShort(v: Short): Short {
        params += v
        return super.visitShort(v)
    }

    override fun visitInt(v: Int): Int {
        params += v
        return super.visitInt(v)
    }

    override fun visitLong(v: Long): Long {
        params += v
        return super.visitLong(v)
    }

    override fun visitFloat(v: Float): Float {
        params += v
        return super.visitFloat(v)
    }

    override fun visitDouble(v: Double): Double {
        params += v
        return super.visitDouble(v)
    }

    override fun visitObject(v: Any?): Any? {
        params += v
        return super.visitObject(v)
    }

    fun collectParameters() = params.toList()
}
