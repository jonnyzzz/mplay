package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import com.jonnyzzz.mplay.agent.config.OpenClassMethodsTask
import org.objectweb.asm.*


interface ClassInterceptor {
    /**
     * @param className - the name of the class in the JVM format (with dots, not slashes)
     */
    fun intercept(className: String, data: ByteArray): ByteArray?
}

fun buildClassInterceptor(config: AgentConfig): ClassInterceptor {
    val allInterceptors =
                config.classesToRecordEvents.map { buildClassInterceptor(config, record = it) } +
                config.classesToOpenMethods.map { buildClassInterceptor(config, open = it) }

    return object : ClassInterceptor {
        override fun intercept(className: String, data: ByteArray) = allInterceptors.mapNotNull {
            it.intercept(className, data)
        }.firstOrNull()
    }
}

private fun buildClassInterceptor(
    config: AgentConfig,
    record: InterceptClassTask? = null,
    open: OpenClassMethodsTask? = null
): ClassInterceptor {
    return object : ClassInterceptor {
        override fun intercept(className: String, data: ByteArray): ByteArray? {
            return when (className) {
                record?.classNameToIntercept -> interceptClass(config, record = record, data = data)
                open?.classNameToIntercept -> interceptClass(config, open = open, data = data)
                else -> null
            }
        }
    }
}

private fun interceptClass(
    config: AgentConfig,
    record: InterceptClassTask? = null,
    open: OpenClassMethodsTask? = null,
    data: ByteArray
): ByteArray {
    val writer = ClassWriter(0)
    val context = ClassPatcherContext()
    var visitor : ClassVisitor = writer

    if (open != null) {
        visitor = ClassPatcherMethodCallOpener(open, visitor)
    }

    if (record != null) {
        visitor = ClassPatcherNameAssert(record, visitor)
        visitor = ClassPatcherRecorderInit(context, config, record, visitor)
        visitor = ClassPatcherMethodCallRecorder(context, record, visitor)
    }

    ClassReader(data).accept(visitor, 0)
    return writer.toByteArray()
}
