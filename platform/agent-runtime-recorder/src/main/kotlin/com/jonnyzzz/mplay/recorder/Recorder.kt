package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.MPlayMethodCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayInstanceRecorder

class RecorderImpl(
    private val perThreadWriter: PerThreadWriter,
    private val instanceId: Int,
) : MPlayInstanceRecorder {
    override fun newMethodRecorder(methodName: String, descriptor: String): MPlayMethodCallRecorder {
        return MethodCallRecorderImpl(
            perThreadWriter.writerForCurrentThread(),
            instanceId,
            methodName,
            descriptor,
        )
    }
}
