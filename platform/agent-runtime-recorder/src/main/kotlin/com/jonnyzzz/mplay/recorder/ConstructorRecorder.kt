package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.MPlayConstructorCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayConstructorRecorder
import com.jonnyzzz.mplay.config.MPlayConfiguration

class ConstructorRecorder(
    private val classloaders: RecorderConfigLoader,
    private val perThreadWriter: PerThreadWriter,
    private var recordingClassName: String,
    private var configClassName: String?,
) : MPlayConstructorRecorder {
    private var instance: Any? = null

    override fun visitInstance(instance: Any) {
        this.instance = instance
        super.visitInstance(instance)
    }

    override fun newConstructorCallRecorder(descriptor: String): MPlayConstructorCallRecorder {
        val instance = instance ?: error("instance is not set")
        val instanceClass = instance.javaClass
        val instanceClassName = instanceClass.name

        if (instanceClassName != recordingClassName) {
            println("MPlay. Recording events for $recordingClassName but has $instanceClassName is ignored")
            return NopRecorder
        }

        //TODO: we need metadata to create configuration class
        val config = configClassName?.let { configClassName ->
            val configClass = try {
                classloaders.loadConfigFor(instance, configClassName)
            } catch (t: Throwable) {
                throw Error("Failed to load MPlay configuration class $configClassName. ${t.message}", t)
            }
            println("MPlay. Loaded config class ${configClass.name} for $recordingClassName")

            run {
                //case 1 - test if this is a Kotlin object
                runCatching {
                    configClass.kotlin.objectInstance?.let { return@run it }
                }

                //case 2 - try to use a constructor
                runCatching {
                    configClass.getConstructor().newInstance().let { return@run it }
                }

                error("Failed to create configuration class ${configClass.name}. The class should have default constructor without parameter or be a Kotlin object")
            } as MPlayConfiguration<*>
        }

        return ConstructorCallRecorderImpl(
            perThreadWriter = perThreadWriter,
            recordingClassName = recordingClassName,
            constructorDescriptor = descriptor,
            config = config,
        )
    }
}
