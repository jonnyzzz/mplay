package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import com.jonnyzzz.mplay.agent.config.InterceptConstructorTask
import com.jonnyzzz.mplay.agent.runtime.MPlayConstructorCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayInstanceRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayValuesVisitor
import com.jonnyzzz.mplay.recorder.json.ConstructorCallMessage
import com.jonnyzzz.mplay.recorder.visit.ParametersToListVisitor
import java.util.concurrent.atomic.AtomicInteger

private val instanceIds = AtomicInteger()

class ConstructorCallRecorderImpl(
    private val interceptClassTask: InterceptClassTask,
    private val ctorTask: InterceptConstructorTask,
    private val perThreadWriter: PerThreadWriter,
    private val config: MPlayConfigAdapter?,

    private val paramsToListVisitor: ParametersToListVisitor = ParametersToListVisitor()
) : MPlayConstructorCallRecorder, MPlayValuesVisitor by paramsToListVisitor {

    override fun newInstanceRecorder(): MPlayInstanceRecorder {
        val args = paramsToListVisitor.collectParameters()
        val driver = config?.resolveDriverCall(args)

        val call = ConstructorCallMessage(
            instanceId = instanceIds.incrementAndGet(),
            recordingClass = interceptClassTask.classNameToIntercept,
            descriptor = ctorTask.methodRef.descriptor,
            parameters = driver?.mapConstructorParamsForSerialization(args) ?: args
        )

        perThreadWriter.writerForCurrentThread().writeConstructorCall(call)

        return InstanceRecorderImpl(
            interceptClassTask = interceptClassTask,
            perThreadWriter = perThreadWriter,
            instanceId = call.instanceId,
            config = config,
        )
    }
}
