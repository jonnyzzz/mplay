package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class ClassPatcherMethodCallRecorder(
    val context: ClassPatcherContext,
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
                var methodRecorderLocalId: Int = -1
                override fun onMethodEnter() {
                    methodRecorderLocalId = newLocal(context.methodCallRecorderType)

                    mv.visitVarInsn(Opcodes.ALOAD, 0)
                    mv.visitFieldInsn(Opcodes.GETFIELD, thisClassJvmName, context.mplayFieldName, context.mplayFieldDescriptor)
                    mv.visitLdcInsn(methodToRecord.methodName)
                    mv.visitLdcInsn(methodToRecord.jvmMethodDescriptor)
                    mv.visitMethodInsn(context.mplayRecorderOnEnter)
                    mv.visitVarInsn(Opcodes.ASTORE, methodRecorderLocalId)
                    //TODO: call the recorder to pass all method parameters
                    super.onMethodEnter()
                }

                override fun onMethodExit(opcode: Int) {
                    //TODO: call the recorder to pass the return/throw parameters if needed
                    mv.visitVarInsn(Opcodes.ALOAD, methodRecorderLocalId)
                    mv.visitMethodInsn(context.methodCallCommitWithResult)
                    super.onMethodExit(opcode)
                }

                override fun visitMaxs(maxStack: Int, maxLocals: Int) {
                    super.visitMaxs(maxStack + 3, maxLocals)
                }
            }
        }

        return methodVisitor
    }
}
