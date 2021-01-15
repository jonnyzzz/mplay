package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.*
import com.jonnyzzz.mplay.recorder.json.*

class MethodResultRecorderImpl(
    private val perThreadWriter: JsonLogWriter,
    private val callId: Long,

    private val paramsToJsonVisitor: ParametersToJsonVisitor = ParametersToJsonVisitor(),
    private val exceptionToValueVisitor: ExceptionToValueVisitor = ExceptionToValueVisitor(),
) : MPlayMethodResultRecorder,
    MPlayValuesVisitor by paramsToJsonVisitor,
    MPlayExceptionVisitor by exceptionToValueVisitor {

    private fun nanoTime() = System.nanoTime()
    private val startTime = nanoTime()

    override fun commit() {
        val duration = (nanoTime() - startTime).coerceAtLeast(0)
        perThreadWriter.writeMethodResult(
            MethodCallResult(
                callId = callId,
                durationNanos = duration,
                result = paramsToJsonVisitor.toJson().takeIf { it.size() > 0 },
                exception = exceptionToValueVisitor.toJson()?.let { ExceptionMessage(it.javaClass.name) },
            )
        )
    }
}
