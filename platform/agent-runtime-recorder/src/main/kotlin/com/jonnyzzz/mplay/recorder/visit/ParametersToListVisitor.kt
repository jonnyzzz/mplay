package com.jonnyzzz.mplay.recorder.visit

import com.jonnyzzz.mplay.agent.runtime.MPlayValuesVisitor

open class ParametersToListVisitor(
    private val base: MPlayValuesVisitor = object : MPlayValuesVisitor {}
) : MPlayValuesVisitor {
    private val params = mutableListOf<Any?>()

    override fun visitBoolean(v: Boolean): Boolean {
        params += v
        return base.visitBoolean(v)
    }

    override fun visitChar(v: Char): Char {
        params += v
        return base.visitChar(v)
    }

    override fun visitByte(v: Byte): Byte {
        params += v
        return base.visitByte(v)
    }

    override fun visitShort(v: Short): Short {
        params += v
        return base.visitShort(v)
    }

    override fun visitInt(v: Int): Int {
        params += v
        return base.visitInt(v)
    }

    override fun visitLong(v: Long): Long {
        params += v
        return base.visitLong(v)
    }

    override fun visitFloat(v: Float): Float {
        params += v
        return base.visitFloat(v)
    }

    override fun visitDouble(v: Double): Double {
        params += v
        return base.visitDouble(v)
    }

    override fun visitObject(v: Any?): Any? {
        params += v
        return base.visitObject(v)
    }

    fun collectParameters() = params.toList()
}