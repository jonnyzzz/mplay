package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorder
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import java.io.File
import java.lang.reflect.Modifier
import kotlin.math.max


class ClassPatcherRecorderInit(
    private val config: AgentConfig,
    private val clazz: InterceptClassTask,
    baseVisitor: ClassVisitor
) :  ClassVisitor(Opcodes.ASM9, baseVisitor) {
    lateinit var jvmClassName : String
    val mplayFieldName get() = "______jonnyzzzMPlayRecorder" // we use unicode symbols to avoid a clash

    private val mplayRecorderType = MPlayRecorder::class.java
    val mplayFieldDescriptor = Type.getDescriptor(mplayRecorderType)
    val mplayTypeInternalName = Type.getInternalName(mplayRecorderType)
    val mplayTypeGetInstancsName = "getInstance"
    val mplayTypeGetInstancsSignature = run {
        val method = mplayRecorderType.methods
            .filter { Modifier.isStatic(it.modifiers) }
            .filter { it.name == mplayTypeGetInstancsName }
            .single()
        Type.getMethodDescriptor(method)
    }

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
            mplayFieldName,
            mplayFieldDescriptor,
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

                   mv.visitMethodInsn(
                       Opcodes.INVOKESTATIC,
                       mplayTypeInternalName,
                       mplayTypeGetInstancsName,
                       mplayTypeGetInstancsSignature,
                       false
                   )

                   mv.visitVarInsn(ALOAD, 0)
                   mv.visitInsn(Opcodes.SWAP)
                   mv.visitFieldInsn(Opcodes.PUTFIELD, jvmClassName, mplayFieldName, mplayFieldDescriptor)
               }

               override fun visitMaxs(maxStack: Int, maxLocals: Int) {
                   super.visitMaxs(max(maxStack, 2 + 3), maxLocals)
               }
           }
        }
        return methodVisitor
    }
}
