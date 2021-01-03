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

interface ClassPatcherRecorderInitInfo {
    val mplayFieldName get() = "______jonnyzzzMPlayRecorder" // we use unicode symbols to avoid a clash

    val mplayRecorderType get() = MPlayRecorder::class.java
    val mplayFieldDescriptor get() = Type.getDescriptor(mplayRecorderType)

    val mplayTypeInternalName get() = Type.getInternalName(mplayRecorderType)

    val mplayRecorderOnMethodEnterMethodName get() = "onMethodEnter"
    val mplayRecorderOnMethodEnterMethod get() = mplayRecorderType.methods
        .filter { it.name == mplayRecorderOnMethodEnterMethodName }
        .filter { !Modifier.isStatic(it.modifiers) && Modifier.isPublic(it.modifiers) }
        .single()

    val mplayRecorderOnMethodEnterMethodSignature get() = Type.getMethodDescriptor(mplayRecorderOnMethodEnterMethod)

    val methodCallRecorderType get() = Type.getType(mplayRecorderOnMethodEnterMethod.returnType)
    val methodCallRecorderInternalName get() = methodCallRecorderType.internalName

}

class ClassPatcherRecorderInit(
    private val config: AgentConfig,
    private val clazz: InterceptClassTask,
    baseVisitor: ClassVisitor
) :  ClassVisitor(Opcodes.ASM9, baseVisitor), ClassPatcherRecorderInitInfo {
    private lateinit var jvmClassName : String
    private val mplayTypeGetInstanceName = "getInstance"
    private val mplayTypeGetInstanceSignature = run {
        val method = mplayRecorderType.methods
            .filter { Modifier.isStatic(it.modifiers) && Modifier.isPublic(it.modifiers) }
            .filter { it.name == mplayTypeGetInstanceName }
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
                       mplayTypeGetInstanceName,
                       mplayTypeGetInstanceSignature,
                       false
                   )

                   mv.visitVarInsn(ALOAD, 0)
                   mv.visitInsn(Opcodes.SWAP)
                   mv.visitFieldInsn(Opcodes.PUTFIELD, jvmClassName, mplayFieldName, mplayFieldDescriptor)
               }

               override fun visitMaxs(maxStack: Int, maxLocals: Int) {
                   super.visitMaxs(max(maxStack, 3), maxLocals)
               }
           }
        }
        return methodVisitor
    }
}
