package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.MPlayExceptionVisitor

class ExceptionToValueVisitor(
    private val base: MPlayExceptionVisitor = object : MPlayExceptionVisitor {}
) : MPlayExceptionVisitor {
    private var result : Throwable? = null

    fun toJson() = result

    override fun visitException(v: Throwable): Throwable {
        result = v
        return base.visitException(v)
    }
}