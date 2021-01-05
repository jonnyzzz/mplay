package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.util.Printer
import org.objectweb.asm.util.Textifier
import org.objectweb.asm.util.TraceMethodVisitor

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
                    super.onMethodEnter()
                    methodRecorderLocalId = newLocal(context.methodCallRecorderType)
                    mv.visitVarInsn(Opcodes.ALOAD, 0)
                    mv.visitFieldInsn(Opcodes.GETFIELD, thisClassJvmName, context.mplayFieldName, context.mplayFieldDescriptor)
                    mv.visitLdcInsn(methodToRecord.methodName)
                    mv.visitLdcInsn(methodToRecord.jvmMethodDescriptor)
                    mv.visitMethodInsn(context.mplayRecorderOnEnter)
                    for ((i, argumentType) in argumentTypes.withIndex()) {
                        dup()
                        loadArg(i)
                        visitMethodInsn(context.mplayWriteMethod(argumentType))
                        storeArg(i)
                    }
                    dup()
                    visitMethodInsn(context.methodCallParametersComplete)
                    mv.visitVarInsn(Opcodes.ASTORE, methodRecorderLocalId)
                }

                override fun onMethodExit(opcode: Int) {
                    super.onMethodExit(opcode)

                    if (Opcodes.ATHROW == opcode) {
                        mv.visitInsn(DUP)
                        mv.visitVarInsn(Opcodes.ALOAD, methodRecorderLocalId)
                        mv.visitInsn(SWAP)
                        mv.visitMethodInsn(context.methodCallCommitWithException)
                        return
                    }

                    if (opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {
                        mv.visitInsn(DUP2)
                        mv.visitVarInsn(Opcodes.ALOAD, methodRecorderLocalId)
                        mv.visitInsn(DUP_X2)
                        mv.visitInsn(POP)
                        mv.visitMethodInsn(context.mplayWriteMethod(returnType))
                    } else if (opcode != Opcodes.RETURN) {
                        mv.visitInsn(DUP)
                        mv.visitVarInsn(Opcodes.ALOAD, methodRecorderLocalId)
                        mv.visitInsn(SWAP)
                        mv.visitMethodInsn(context.mplayWriteMethod(returnType))
                    }

                    mv.visitVarInsn(Opcodes.ALOAD, methodRecorderLocalId)
                    mv.visitMethodInsn(context.methodCallCommitWithResult)
                }

                override fun visitMaxs(maxStack: Int, maxLocals: Int) {
                    super.visitMaxs(maxStack + 3, maxLocals)
                }
            }

        }

        return methodVisitor
    }
}
