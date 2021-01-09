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
    return object : ClassInterceptor {
        override fun intercept(className: String, data: ByteArray): ByteArray? {
            println("MPlay instrumentation checking $className")

            var patched = data
            for (open in config.classesToOpenMethods) {
                if (open.classNameToIntercept == className) {
                    patched = interceptClass(config, open = open, data = patched)
                }
            }

            for (record in config.classesToRecordEvents) {
                if (record.classNameToIntercept == className) {
                    patched = interceptClass(config, record = record, data = patched)
                }
            }

            return when {
                patched.contentEquals(data) -> null
                else -> {
                    println("MPlay instrumented $className")
                    patched
                }
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
    try {
        val writer = ClassWriter(0)
        val context = ClassPatcherContext()
        var visitor: ClassVisitor = writer

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
    } catch (t: Throwable) {
        System.err.println("\n\nMPlay Agent. Failed to instrument class" +
                " ${record?.classNameToIntercept ?: open?.classNameToIntercept}")
        t.printStackTrace(System.err)
        return data
    }
}
