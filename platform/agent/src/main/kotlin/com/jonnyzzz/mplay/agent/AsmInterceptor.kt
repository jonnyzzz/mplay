package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter


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
            return interceptClass(config, clazz, className, data)
        }
    }
}

private fun interceptClass(
    config: AgentConfig,
    clazz: InterceptClassTask,
    className: String,
    data: ByteArray
): ByteArray {
    val writer = ClassWriter(0)
    val visitor = object : ClassVisitor(Opcodes.ASM9, writer) {
        private lateinit var jvmNativeClassName: String
        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            jvmNativeClassName = name
            require(name.replace('/', '.') == className) {
                "Instrumenting class $name but expected $className"
            }

            super.visit(version, access, name, signature, superName, interfaces)
        }

        override fun visitMethod(
            access: Int,
            name: String,
            descriptor: String?,
            signature: String?,
            exceptions: Array<out String>?
        ): MethodVisitor? {
            when (name) {
                "<init>" -> println("Visiting constructor of $className - $name - $descriptor - $signature")
                "<clinit>" -> println("Visiting static constructor of $className - $name - $descriptor - $signature")
                else -> println("Visiting method of $className - $name - $descriptor - $signature")
            }

            var methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions) ?: return null

            val methodToRecord = clazz.methodsToRecord
                .firstOrNull { it.methodName == name && it.jvmMethodDescriptor == descriptor }

            if (methodToRecord != null) {
                println("Intercepting method $name with $signature to record calls")

                methodVisitor = object : AdviceAdapter(Opcodes.ASM9, methodVisitor, access, name, descriptor) {
                    override fun onMethodEnter() {
                        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
                        mv.visitLdcInsn("Instrumented method enter: $name -- $signature -- $descriptor")
                        mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "java/io/PrintStream",
                            "println",
                            "(Ljava/lang/String;)V",
                            false
                        )

                        super.onMethodEnter()
                    }

                    override fun onMethodExit(opcode: Int) {
                        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
                        mv.visitLdcInsn("Instrumented method exit: code: $opcode - $name -- $signature -- $descriptor")
                        mv.visitMethodInsn(
                            Opcodes.INVOKEVIRTUAL,
                            "java/io/PrintStream",
                            "println",
                            "(Ljava/lang/String;)V",
                            false
                        )

                        super.onMethodExit(opcode)
                    }

                    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
                        super.visitMaxs(maxStack + 2, maxLocals)
                    }
                }
            }

            return methodVisitor
        }
    }
    ClassReader(data).accept(visitor, 0)
    return writer.toByteArray()
}
