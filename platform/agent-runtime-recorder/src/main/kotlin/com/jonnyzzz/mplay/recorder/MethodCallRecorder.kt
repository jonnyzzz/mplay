package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.MPlayMethodCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayRunningMethodRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayValuesVisitor
import com.jonnyzzz.mplay.recorder.json.JsonLogWriter
import com.jonnyzzz.mplay.recorder.json.MethodCallMessage

class MethodCallRecorderImpl(
    private val perThreadWriter: JsonLogWriter,
    private val instanceId: Int,
    private val methodName: String,
    private val descriptor: String,

    private val paramsToListVisitor: ParametersToListVisitor = ParametersToListVisitor()
) : MPlayMethodCallRecorder, MPlayValuesVisitor by paramsToListVisitor {
    override fun newRunningMethodRecorder(): MPlayRunningMethodRecorder {
        val callId = MethodCallLocal.tryRegisterNextCall() ?: run {
            // null means we are already recording a method call
            // on the stack - there is no need to record yet another
            // one recursively.
            return NopRecorder
        }

        val call = MethodCallMessage(
            callId = callId,
            instanceId = instanceId,
            name = methodName,
            descriptor = descriptor,
            parameters = paramsToListVisitor.collectParameters(),
        )
        perThreadWriter.writeMethodCall(call)

        return MethodResultRecorderImpl(perThreadWriter, call.callId)
    }
}
