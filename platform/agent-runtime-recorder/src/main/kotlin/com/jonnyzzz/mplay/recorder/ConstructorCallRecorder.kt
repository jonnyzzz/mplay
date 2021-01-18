package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import com.jonnyzzz.mplay.agent.config.InterceptConstructorTask
import com.jonnyzzz.mplay.agent.runtime.MPlayConstructorCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayInstanceRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayValuesVisitor
import com.jonnyzzz.mplay.config.MPlayConfiguration
import com.jonnyzzz.mplay.recorder.json.ConstructorCallMessage
import com.jonnyzzz.mplay.recorder.visit.ParametersToListVisitor
import java.util.concurrent.atomic.AtomicInteger

private val instanceIds = AtomicInteger()

class ConstructorCallRecorderImpl(
    private val interceptClassTask: InterceptClassTask,
    private val ctorTask: InterceptConstructorTask,
    private val perThreadWriter: PerThreadWriter,
    private val config: MPlayConfiguration<*>?,

    private val paramsToListVisitor: ParametersToListVisitor = ParametersToListVisitor()
) : MPlayConstructorCallRecorder, MPlayValuesVisitor by paramsToListVisitor {

    override fun newInstanceRecorder(): MPlayInstanceRecorder {
//        val config = configClass?.let { configClass ->
//            val constructorParameters = paramsToListVisitor.collectParameters()
//            val config = configClass.constructors
//                .filter { it.parameterCount == constructorParameters.size }
//                .single() //TODO: we could make it smarter here, moreover
//                .newInstance(*constructorParameters.toTypedArray()) as MPlayConfiguration<*>
//            println("MPlay. Config class ${configClass.name} created")
//        }

        val call = ConstructorCallMessage(
            instanceId = instanceIds.incrementAndGet(),
            recordingClass = interceptClassTask.classNameToIntercept,
            descriptor = ctorTask.methodRef.descriptor,
            parameters = paramsToListVisitor.collectParameters() //TODO: configuration may affect the way parameters are recorded
        )

        perThreadWriter.writerForCurrentThread().writeConstructorCall(call)

        //TODO: select constructor to call here via the metadata

        return InstanceRecorderImpl(
            interceptClassTask = interceptClassTask,
            perThreadWriter = perThreadWriter,
            instanceId = call.instanceId
        )
    }
}
