package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
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
                    super.onMethodEnter()
                    loadThis()
                    visitFieldInsn(Opcodes.GETFIELD, thisClassJvmName, context.mplayFieldName, context.mplayFieldDescriptor)
                    visitLdcInsn(methodToRecord.methodName)
                    visitLdcInsn(methodToRecord.jvmMethodDescriptor)
                    visitMethodInsn(context.mplayRecorderOnEnter)

                    for ((i, argumentType) in argumentTypes.withIndex()) {
                        dup()
                        loadArg(i)
                        visitMethodInsn(context.mplayVisitMethod(argumentType))
                        storeArg(i)
                    }
                    dup()
                    visitMethodInsn(context.methodCallParametersComplete)
                    methodRecorderLocalId = newLocal(Type.getType(context.methodCallParametersComplete.returnType))
                    mv.visitVarInsn(Opcodes.ASTORE, methodRecorderLocalId)
                }

                override fun onMethodExit(opcode: Int) {
                    super.onMethodExit(opcode)

                    if (Opcodes.ATHROW == opcode) {
                        dup()
                        mv.visitVarInsn(Opcodes.ALOAD, methodRecorderLocalId)
                        swap()
                        visitMethodInsn(context.visitException)
                    } else if (opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {
                        dup2()
                        mv.visitVarInsn(Opcodes.ALOAD, methodRecorderLocalId)
                        visitInsn(DUP_X2)
                        visitInsn(POP)
                        visitMethodInsn(context.mplayVisitMethod(returnType))
                    } else if (opcode != Opcodes.RETURN) {
                        dup()
                        mv.visitVarInsn(Opcodes.ALOAD, methodRecorderLocalId)
                        swap()
                        visitMethodInsn(context.mplayVisitMethod(returnType))
                    }

                    mv.visitVarInsn(Opcodes.ALOAD, methodRecorderLocalId)
                    mv.visitMethodInsn(context.methodCallCommit)
                }

                override fun visitMaxs(maxStack: Int, maxLocals: Int) {
                    super.visitMaxs(maxStack + 3, maxLocals)
                }
            }

        }

        return methodVisitor
    }
}
