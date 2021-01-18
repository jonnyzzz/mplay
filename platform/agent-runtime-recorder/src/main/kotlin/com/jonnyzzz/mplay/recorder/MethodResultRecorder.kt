package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.*
import com.jonnyzzz.mplay.recorder.json.*

class MethodResultRecorderImpl(
    private val perThreadWriter: JsonLogWriter,
    private val callId: Long,

    private val paramsToJsonVisitor: ParametersToListVisitor = ParametersToListVisitor(),
    private val exceptionToValueVisitor: ExceptionToValueVisitor = ExceptionToValueVisitor(),
) : MPlayRunningMethodRecorder,
    MPlayMethodResultRecorder,
    MPlayValuesVisitor by paramsToJsonVisitor,
    MPlayExceptionVisitor by exceptionToValueVisitor {

    private fun nanoTime() = System.nanoTime()
    private val startTime = nanoTime()
    private var finishTime: Long = -1

    override fun newMethodResultRecorder(): MPlayMethodResultRecorder {
        finishTime = nanoTime()
        return this
    }

    override fun commit() {
        val duration = (finishTime - startTime).coerceAtLeast(0)
        perThreadWriter.writeMethodResult(
            MethodCallResult(
                callId = callId,
                durationNanos = duration,
                result = paramsToJsonVisitor.collectParameters().singleOrNull(),
                exception = exceptionToValueVisitor.toJson()?.let { ExceptionMessage(it.javaClass.name) },
            )
        )
    }
}
