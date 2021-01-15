package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.*
import com.jonnyzzz.mplay.recorder.json.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

private val instanceIds = AtomicInteger()

class RecorderBuilderImpl(
    private val classloaders: RecorderConfigLoader,
    private val perThreadWriter: PerThreadWriter,

    private val paramsToJsonVisitor: ParametersToJsonVisitor = ParametersToJsonVisitor(),
    private val paramsToListVisitor: ParametersToListVisitor = ParametersToListVisitor(paramsToJsonVisitor)
) : MPlayRecorderBuilder, MPlayValuesVisitor by paramsToListVisitor {
    private var recordingClassName: String? = null
    private var configurationClassName: String? = null
    private var constructorDescriptor: String? = null
    private var instance: Any? = null

    override fun visitRecordingClassName(recordingClassName: String) {
        this.recordingClassName = recordingClassName
        super.visitRecordingClassName(recordingClassName)
    }

    override fun visitConfigurationClassName(configurationClassName: String) {
        this.configurationClassName = configurationClassName
        super.visitConfigurationClassName(configurationClassName)
    }

    override fun visitConstructorDescriptor(descriptor: String) {
        this.constructorDescriptor = descriptor
        super.visitConstructorDescriptor(descriptor)
    }

    override fun visitInstance(instance: Any) {
        this.instance = instance
        super.visitInstance(instance)
    }

    override fun visitConstructorParametersComplete(): MPlayRecorder {
        val recordingClassName = recordingClassName ?: error("recording class name is not set")
        val configClassName = configurationClassName
        val constructorDescriptor = constructorDescriptor ?: error("constructor is not set")
        val instance = instance ?: error("instance is not set")
        val instanceClass = instance.javaClass
        val instanceClassName = instanceClass.name

        if (instanceClassName != recordingClassName) {
            println("MPlay. Recording events for $recordingClassName but has $instanceClassName is ignored")
            return NopRecorder
        }

        //there should be a map from descriptor to specific constructor parameter or method
        //this could could be generated somewhere to avoid tons of reflections
        //on the other hand, method handles can help (or generated dynamic invoke LOL)

        //TODO: we need metadata to create configuration class
        if (configClassName != null) {
            val configClass = try {
                classloaders.loadConfigFor(instance, configClassName)
            } catch (t: Throwable) {
                throw Error("Failed to load MPlay configuration class $configClassName. ${t.message}", t)
            }
            println("MPlay. Loaded config class ${configClass.name} for $recordingClassName")

            val constructorParameters = paramsToListVisitor.collectParameters()
            val config = configClass.constructors
                .filter { it.parameterCount == constructorParameters.size }
                .single() //TODO: we could make it smarter here, moreover
                .newInstance(*constructorParameters.toTypedArray())

            println("MPlay. Config class ${configClass.name} created")
        }

        val call = ConstructorCallMessage(
            instanceId = instanceIds.incrementAndGet(),
            recordingClass = recordingClassName,
            descriptor = constructorDescriptor,
            parameters = paramsToJsonVisitor.toJson() //TODO: configuration may affect the way parameres are recorded
        )

        perThreadWriter.writerForCurrentThread().writeConstructorCall(call)

        //TODO: select constructor to call here via the metadata

        return RecorderImpl(perThreadWriter, call.instanceId)
    }
}

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

private val callIds = AtomicLong()

class MethodCallRecorderImpl(
    private val perThreadWriter: JsonLogWriter,
    private val instanceId: Int,
    private val methodName: String,
    private val descriptor: String,

    private val paramsToJsonVisitor: ParametersToJsonVisitor = ParametersToJsonVisitor(),
) : MPlayMethodCallRecorder, MPlayValuesVisitor by paramsToJsonVisitor {
    override fun visitParametersComplete(): MethodResultRecorderImpl {
        val call = MethodCallMessage(
            callId = callIds.incrementAndGet(),
            instanceId = instanceId,
            name = methodName,
            descriptor = descriptor,
            parameters = paramsToJsonVisitor.toJson(),
        )
        perThreadWriter.writeMethodCall(call)

        return MethodResultRecorderImpl(perThreadWriter, call.callId)
    }
}

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
