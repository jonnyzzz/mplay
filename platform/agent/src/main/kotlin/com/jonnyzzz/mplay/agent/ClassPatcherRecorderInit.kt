package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorder
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

class ClassPatcherRecorderInit(
    private val clazz: InterceptClassTask,
    baseVisitor: ClassVisitor
) :  ClassVisitor(Opcodes.ASM9, baseVisitor) {
    lateinit var jvmClassName : String
    val mplayFieldName get() = "______jonnyzzzMPlayRecorder" // we use unicode symbols to avoid a clash
    val mplayFieldDescriptor = Type.getDescriptor(MPlayRecorder::class.java)
    val mplayTypeInternalName = Type.getInternalName(MPlayRecorder::class.java)


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
                   //used later for the PUTFIELD instruction
                   mv.visitVarInsn(ALOAD, 0)

                   mv.visitTypeInsn(Opcodes.NEW, mplayTypeInternalName)
                   mv.visitInsn(Opcodes.DUP)
                   mv.visitMethodInsn(Opcodes.INVOKESPECIAL, mplayTypeInternalName, "<init>", "()V", false)

                   mv.visitFieldInsn(Opcodes.PUTFIELD, jvmClassName, mplayFieldName, mplayFieldDescriptor)
               }

               override fun visitMaxs(maxStack: Int, maxLocals: Int) {
                   super.visitMaxs(maxStack + 2, maxLocals)
               }
           }
        }
        return methodVisitor
    }
}
