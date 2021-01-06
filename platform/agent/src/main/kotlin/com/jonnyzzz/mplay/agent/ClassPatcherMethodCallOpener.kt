package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.MethodRef
import com.jonnyzzz.mplay.agent.config.OpenClassMethodsTask
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ClassPatcherMethodCallOpener(
    private val clazz: OpenClassMethodsTask,
    baseVisitor: ClassVisitor
) :  ClassVisitor(Opcodes.ASM9, baseVisitor) {
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        val methodToRecord = clazz.methodsToOpen
            .firstOrNull { it.methodRef == MethodRef(name, descriptor) }?.methodRef

        @Suppress("NAME_SHADOWING")
        var access = access
        if (methodToRecord != null) {
            access = access and Opcodes.ACC_FINAL.inv()
            println("Removing final from method $name with $signature in ${clazz.classNameToIntercept}")
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }
}
