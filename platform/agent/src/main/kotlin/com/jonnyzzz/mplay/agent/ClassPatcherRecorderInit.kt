package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter
import java.io.File
import kotlin.math.max

class ClassPatcherRecorderInit(
    private val context: ClassPatcherContext,
    private val config: AgentConfig,
    private val clazz: InterceptClassTask,
    baseVisitor: ClassVisitor
) :  ClassVisitor(Opcodes.ASM9, baseVisitor) {
    private lateinit var jvmClassName : String

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        jvmClassName = name
        cv.visitField(Opcodes.ACC_FINAL or Opcodes.ACC_PRIVATE,
            context.mplayFieldName,
            context.mplayFieldDescriptor,
            null,
            null)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        var methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        if (name == "<init>") {
           methodVisitor = object: AdviceAdapter(api, methodVisitor, access, name, descriptor) {
               override fun onMethodEnter() {
                   mv.visitLdcInsn(clazz.classNameToIntercept)
                   mv.visitLdcInsn(clazz.configClassName)
                   mv.visitLdcInsn(config.configClasspath.distinct().joinToString(File.separator))
                   mv.visitMethodInsn(context.mplayStaticGetInstance)

                   mv.visitVarInsn(ALOAD, 0)
                   mv.visitInsn(Opcodes.SWAP)
                   mv.visitFieldInsn(Opcodes.PUTFIELD, jvmClassName, context.mplayFieldName, context.mplayFieldDescriptor)
               }

               override fun visitMaxs(maxStack: Int, maxLocals: Int) {
                   super.visitMaxs(max(maxStack, 3), maxLocals)
               }
           }
        }
        return methodVisitor
    }
}
