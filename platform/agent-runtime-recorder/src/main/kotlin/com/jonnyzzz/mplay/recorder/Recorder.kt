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

            val constructorParameters = collectParameters()
            val config = configClass.constructors
                .filter { it.parameterCount == constructorParameters.size }
                .single() //TODO: we could make it smarter here, moreover
                .newInstance(*constructorParameters.toTypedArray())

            println("MPlay. Config class ${configClass.name} created")
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
