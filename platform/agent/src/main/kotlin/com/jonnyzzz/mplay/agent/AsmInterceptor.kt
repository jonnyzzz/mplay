package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import org.objectweb.asm.*


interface ClassInterceptor {
    /**
     * @param className - the name of the class in the JVM format (with dots, not slashes)
     */
    fun intercept(className: String, data: ByteArray): ByteArray?
}

fun buildClassInterceptor(config: AgentConfig): ClassInterceptor {
    val allInterceptors = config.classesToRecordEvents.map { buildClassInterceptor(config, it) }
    return object : ClassInterceptor {
        override fun intercept(className: String, data: ByteArray) = allInterceptors.mapNotNull {
            it.intercept(className, data)
        }.firstOrNull()
    }
}

private fun buildClassInterceptor(config: AgentConfig, clazz: InterceptClassTask): ClassInterceptor {
    return object : ClassInterceptor {
        override fun intercept(className: String, data: ByteArray): ByteArray? {
            if (className != clazz.configClassName) return null
            return interceptClass(config, clazz, data)
        }
    }
}

private fun interceptClass(
    config: AgentConfig,
    clazz: InterceptClassTask,
    data: ByteArray
): ByteArray {
    val writer = ClassWriter(0)
    val context = ClassPatcherContext()
    var visitor : ClassVisitor = writer
    visitor = ClassPatcherNameAssert(clazz, visitor)
    visitor = ClassPatcherRecorderInit(context, config, clazz, visitor)
    visitor = ClassPatcherMethodCallRecorder(context, clazz, visitor)
    ClassReader(data).accept(visitor, 0)
    return writer.toByteArray()
}
