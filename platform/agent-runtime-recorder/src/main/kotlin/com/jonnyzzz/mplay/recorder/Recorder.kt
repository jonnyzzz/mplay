package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.MPlayMethodCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorder

class RecorderImpl(
    private val perThreadWriter: PerThreadWriter,
    private val instanceId: Int,
) : MPlayRecorder {
    override fun onMethodEnter(methodName: String, jvmMethodDescriptor: String): MPlayMethodCallRecorder {
        return MethodCallRecorderImpl(
            perThreadWriter.writerForCurrentThread(),
            instanceId,
            methodName,
            jvmMethodDescriptor,
        )
    }
}
