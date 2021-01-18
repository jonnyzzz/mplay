package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import com.jonnyzzz.mplay.agent.config.InterceptMethodTask
import com.jonnyzzz.mplay.agent.config.MethodRef
import com.jonnyzzz.mplay.agent.runtime.MPlayMethodCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayInstanceRecorder

class InstanceRecorderImpl(
    private val interceptClassTask: InterceptClassTask,
    private val perThreadWriter: PerThreadWriter,
    private val instanceId: Int,
) : MPlayInstanceRecorder {
    override fun newMethodRecorder(methodName: String, descriptor: String): MPlayMethodCallRecorder {
        val methodTask: InterceptMethodTask = interceptClassTask.methodsToRecord.singleOrNull {
            it.methodRef == MethodRef(methodName, descriptor)
        } ?: return NopRecorder

        val callId = MethodCallLocal.tryRegisterNextCall() ?: run {
            // null means we are already recording a method call
            // on the stack - there is no need to record yet another
            // one recursively.
            return NopRecorder
        }

        return MethodCallRecorderImpl(
            methodTask = methodTask,
            perThreadWriter = perThreadWriter.writerForCurrentThread(),
            instanceId = instanceId,
            callId = callId,
        )
    }
}
