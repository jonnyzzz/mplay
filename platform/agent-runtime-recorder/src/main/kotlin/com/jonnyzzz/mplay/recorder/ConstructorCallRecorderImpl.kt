package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.MPlayConstructorCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayInstanceRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayValuesVisitor
import com.jonnyzzz.mplay.config.MPlayConfiguration
import com.jonnyzzz.mplay.recorder.json.ConstructorCallMessage
import java.util.concurrent.atomic.AtomicInteger

private val instanceIds = AtomicInteger()

class ConstructorCallRecorderImpl(
    private val perThreadWriter: PerThreadWriter,
    private val recordingClassName : String,
    private val constructorDescriptor: String,
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
            recordingClass = recordingClassName,
            descriptor = constructorDescriptor,
            parameters = paramsToListVisitor.collectParameters() //TODO: configuration may affect the way parameres are recorded
        )

        perThreadWriter.writerForCurrentThread().writeConstructorCall(call)

        //TODO: select constructor to call here via the metadata

        return RecorderImpl(perThreadWriter, call.instanceId)
    }
}
