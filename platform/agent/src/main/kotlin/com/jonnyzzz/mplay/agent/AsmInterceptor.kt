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

            val writer = ClassWriter(0)
            val visitor = object : ClassVisitor(Opcodes.ASM9, writer) {
                override fun visitMethod(
                    access: Int,
                    name: String?,
                    descriptor: String?,
                    signature: String?,
                    exceptions: Array<out String>?
                ): MethodVisitor? {
                    if (name == "<init>") {
                        println("Visiting constructor of $className - $name - $descriptor - $signature")
                    } else {
                        println("Visiting method of $className - $name - $descriptor - $signature")
                    }

                    val baseVisitor = super.visitMethod(access, name, descriptor, signature, exceptions) ?: return null
                    return object : MethodVisitor(Opcodes.ASM9, baseVisitor) {
                        override fun visitCode() {
                            super.visitCode()

                            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
                            mv.visitLdcInsn("Instrumented method: $name")
                            mv.visitMethodInsn(
                                Opcodes.INVOKEVIRTUAL,
                                "java/io/PrintStream",
                                "println",
                                "(Ljava/lang/String;)V",
                                false
                            )
                        }

                        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
                            super.visitMaxs(maxStack + 2, maxLocals + 1)
                        }
                    }
                }
            }
            ClassReader(data).accept(visitor, 0)
            return writer.toByteArray()
        }
    }
}
