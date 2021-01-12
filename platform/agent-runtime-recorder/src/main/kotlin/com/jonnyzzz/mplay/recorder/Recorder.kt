package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.*

class RecorderBuilderImpl(
    private val classloaders: RecorderConfigLoader
) : ParametersToListVisitor(), MPlayRecorderBuilder {
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

        //TODO: we need metadata to create configuration class
        if (configClassName != null) {
            val configClass = classloaders.loadConfigFor(instance, configClassName)
            println("MPlay. Created config class ${configClass.name} for $recordingClassName")
        }

        //TODO: select constructor to call here via the metadata

        return RecorderImpl()
    }
}

class RecorderImpl : MPlayRecorder {
    override fun onMethodEnter(methodName: String, jvmMethodDescriptor: String): MPlayMethodCallRecorder {
        return MethodCallRecorderImpl()
    }
}

class MethodCallRecorderImpl : MPlayMethodCallRecorder {
    override fun visitParametersComplete(): MethodResultRecorderImpl {
        return MethodResultRecorderImpl()
    }
}

class MethodResultRecorderImpl : MPlayMethodResultRecorder {
    override fun commit() {
    }
}
