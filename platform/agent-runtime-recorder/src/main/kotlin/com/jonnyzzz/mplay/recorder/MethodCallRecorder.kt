package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.config.InterceptMethodTask
import com.jonnyzzz.mplay.agent.runtime.MPlayMethodCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayRunningMethodRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayValuesVisitor
import com.jonnyzzz.mplay.recorder.json.JsonLogWriter
import com.jonnyzzz.mplay.recorder.json.MethodCallMessage
import com.jonnyzzz.mplay.recorder.visit.ParametersToListVisitor

class MethodCallRecorderImpl(
    private val methodTask: InterceptMethodTask,
    private val perThreadWriter: JsonLogWriter,
    private val instanceId: Int,
    private val callId: Long,

    private val paramsToListVisitor: ParametersToListVisitor = ParametersToListVisitor()
) : MPlayMethodCallRecorder, MPlayValuesVisitor by paramsToListVisitor {
    override fun newRunningMethodRecorder(): MPlayRunningMethodRecorder {
        val call = MethodCallMessage(
            callId = callId,
            instanceId = instanceId,
            name = methodTask.methodRef.methodName,
            descriptor = methodTask.methodRef.descriptor,
            parameters = paramsToListVisitor.collectParameters(),
        )
        perThreadWriter.writeMethodCall(call)

        return MethodResultRecorderImpl(
            methodTask = methodTask,
            perThreadWriter = perThreadWriter,
            callId = call.callId
        )
    }
}
