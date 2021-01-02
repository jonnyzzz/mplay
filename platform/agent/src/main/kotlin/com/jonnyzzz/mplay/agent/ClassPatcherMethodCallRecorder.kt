package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class ClassPatcherMethodCallRecorder(
    val recorderInfo: ClassPatcherRecorderInitInfo,
    private val clazz: InterceptClassTask,
    baseVisitor: ClassVisitor
) :  ClassVisitor(Opcodes.ASM9, baseVisitor) {
    private lateinit var thisClassJvmName: String

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        thisClassJvmName = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        var methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions) ?: return null

        val methodToRecord = clazz.methodsToRecord
            .firstOrNull { it.methodName == name && it.jvmMethodDescriptor == descriptor }

        if (methodToRecord != null) {
            println("Intercepting method $name with $signature to record calls")

            methodVisitor = object : AdviceAdapter(ASM9, methodVisitor, access, name, descriptor) {
                override fun onMethodEnter() {

                    mv.visitVarInsn(Opcodes.ALOAD, 0)
                    mv.visitFieldInsn(Opcodes.GETFIELD, thisClassJvmName, recorderInfo.mplayFieldName, recorderInfo.mplayFieldDescriptor)
                    mv.visitLdcInsn(name)
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, recorderInfo.mplayTypeInternalName, "onMethodEnter", "(Ljava/lang/String;)V", false)

                    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
                    mv.visitLdcInsn("Instrumented method enter: $name -- $signature -- $descriptor")
                    mv.visitMethodInsn(
                        INVOKEVIRTUAL,
                        "java/io/PrintStream",
                        "println",
                        "(Ljava/lang/String;)V",
                        false
                    )

                    super.onMethodEnter()
                }

                override fun onMethodExit(opcode: Int) {
                    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
                    mv.visitLdcInsn("Instrumented method exit: code: $opcode - $name -- $signature -- $descriptor")
                    mv.visitMethodInsn(
                        INVOKEVIRTUAL,
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