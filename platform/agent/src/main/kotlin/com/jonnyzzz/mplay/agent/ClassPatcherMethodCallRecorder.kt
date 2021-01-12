package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import com.jonnyzzz.mplay.agent.config.InterceptMethodTask
import com.jonnyzzz.mplay.agent.config.MethodRef
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.GeneratorAdapter

class ClassPatcherMethodCallRecorder(
    val context: ClassPatcherContext,
    private val clazz: InterceptClassTask,
    baseVisitor: ClassVisitor
) :  ClassVisitor(Opcodes.ASM9, baseVisitor) {
    private lateinit var thisClassJvmName: String
    private var thisClassBaseJvmName: String? = null

    private val visitedMethods = mutableSetOf<InterceptMethodTask>()

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        thisClassJvmName = name
        thisClassBaseJvmName = superName
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        var methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions) ?: return null

        val methodToRecord = clazz.methodsToRecord
            .firstOrNull { it.methodRef == MethodRef(name, descriptor) }

        if (methodToRecord != null) {
            println("Intercepting method $name with $descriptor to record calls")

            visitedMethods += methodToRecord
            methodVisitor = recordMethodCallAdvice(methodVisitor, access, name, descriptor, methodToRecord.methodRef)
        }

        return methodVisitor
    }

    override fun visitEnd() {
        //for all types from the base classes
        for (methodToImplement in clazz.methodsToRecord.filter { it !in visitedMethods }.toList()) {
            val ref = methodToImplement.methodRef
            val access = Opcodes.ACC_PUBLIC or Opcodes.ACC_SYNTHETIC
            val baseVisitor = visitMethod(
                access,
                ref.methodName,
                ref.descriptor,
                null, //TODO
                arrayOf() //TODO
            ) ?: continue

            val mv = GeneratorAdapter(baseVisitor, access, ref.methodName, ref.descriptor)

            mv.visitCode()
            mv.loadThis()
            for (arg in mv.argumentTypes.indices) {
                mv.loadArg(arg)
            }

            val owner = methodToImplement.defaultMethodOfInterface?.replace('.', '/') ?: thisClassBaseJvmName
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, ref.methodName, ref.descriptor, methodToImplement.defaultMethodOfInterface != null)
            val sz = mv.argumentTypes.sumBy { it.size }
            mv.returnValue()

            mv.visitMaxs(sz + 1, sz + 1)
            mv.visitEnd()
        }

        super.visitEnd()
    }

    private fun recordMethodCallAdvice(
        baseVisitor: MethodVisitor,
        access: Int,
        name: String,
        descriptor: String,
        methodToRecord: MethodRef
    ) = object : AdviceAdapter(ASM9, baseVisitor, access, name, descriptor) {
        var methodRecorderLocalId: Int = -1
        override fun onMethodEnter() {
            super.onMethodEnter()
            loadThis()
            visitFieldInsn(GETFIELD, thisClassJvmName, context.mplayFieldName, context.mplayFieldDescriptor)
            visitLdcInsn(methodToRecord.methodName)
            visitLdcInsn(methodToRecord.descriptor)
            visitMethodInsn(context.mplayRecorderOnEnter)

            for ((i, argumentType) in argumentTypes.withIndex()) {
                dup()
                loadArg(i)
                visitMethodInsn(context.mplayVisitMethod(argumentType))
                if (argumentType.sort == Type.OBJECT || argumentType.sort == Type.ARRAY) {
                    checkCast(argumentType)
                }
                storeArg(i)
            }
            dup()
            visitMethodInsn(context.methodCallParametersComplete)
            methodRecorderLocalId = newLocal(Type.getType(context.methodCallParametersComplete.returnType))
            mv.visitVarInsn(ASTORE, methodRecorderLocalId)
        }

        override fun onMethodExit(opcode: Int) {
            super.onMethodExit(opcode)

            if (ATHROW == opcode) {
                dup()
                mv.visitVarInsn(ALOAD, methodRecorderLocalId)
                swap()
                visitMethodInsn(context.visitException)
            } else if (opcode == LRETURN || opcode == DRETURN) {
                dup2()
                mv.visitVarInsn(ALOAD, methodRecorderLocalId)
                visitInsn(DUP_X2)
                visitInsn(POP)
                visitMethodInsn(context.mplayVisitMethod(returnType))
            } else if (opcode != RETURN) {
                dup()
                mv.visitVarInsn(ALOAD, methodRecorderLocalId)
                swap()
                visitMethodInsn(context.mplayVisitMethod(returnType))
            }

            mv.visitVarInsn(ALOAD, methodRecorderLocalId)
            mv.visitMethodInsn(context.methodCallCommit)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            super.visitMaxs(maxStack + 5, maxLocals)
        }
    }
}
