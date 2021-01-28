package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import com.jonnyzzz.mplay.agent.config.InterceptConstructorTask
import com.jonnyzzz.mplay.agent.config.MethodRef
import com.jonnyzzz.mplay.agent.config.ctor
import com.jonnyzzz.mplay.agent.runtime.MPlayConstructorCallRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayConstructorRecorder
import com.jonnyzzz.mplay.config.MPlayConfiguration

class ConstructorRecorder(
    private val interceptClassTask: InterceptClassTask,
    private val classloaders: RecorderConfigLoader,
    private val perThreadWriter: PerThreadWriter,
) : MPlayConstructorRecorder {
    private var instance: Any? = null

    override fun visitInstance(instance: Any) {
        this.instance = instance
        super.visitInstance(instance)
    }

    override fun newConstructorCallRecorder(descriptor: String): MPlayConstructorCallRecorder {
        val ctorTask: InterceptConstructorTask = interceptClassTask.constructorsToIntercept.singleOrNull {
            it.methodRef == MethodRef.ctor(descriptor)
        } ?: return NopRecorder

        val recordingClassName: String = interceptClassTask.classNameToIntercept
        val configClassName: String? = interceptClassTask.configClassName

        val instance = instance ?: error("instance is not set for <init> $descriptor in $interceptClassTask")
        val instanceClass = instance.javaClass
        val instanceClassName = instanceClass.name

        if (instanceClassName != recordingClassName) {
            println("MPlay. Recording events for $recordingClassName but has $instanceClassName is ignored")
            return NopRecorder
        }

        val config: MPlayConfigAdapter? = /*configClassName
            ?.let { newConfigClass(instance, it) }
            ?.let { MPlayConfigAdapter(it, descriptor) }
            */ null

        return ConstructorCallRecorderImpl(
            interceptClassTask = interceptClassTask,
            ctorTask = ctorTask,
            perThreadWriter = perThreadWriter,
            config = config,
        )
    }

    private fun newConfigClass(instance: Any, configClassName: String): MPlayConfiguration<*> {
        val configClass = try {
            classloaders.loadConfigFor(instance, configClassName)
        } catch (t: Throwable) {
            throw Error("Failed to load MPlay configuration class $configClassName. ${t.message}", t)
        }
        println("MPlay. Loaded config class ${configClass.name} for $interceptClassTask")

        return run {
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
}
