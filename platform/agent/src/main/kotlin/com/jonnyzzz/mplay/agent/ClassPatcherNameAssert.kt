package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class ClassPatcherNameAssert(
    private val clazz: InterceptClassTask,
    baseVisitor: ClassVisitor
) : ClassVisitor(Opcodes.ASM9, baseVisitor) {
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        require(name.replace('/', '.') == clazz.classNameToIntercept) {
            "Instrumenting class $name but expected ${clazz.classNameToIntercept}"
        }

        super.visit(version, access, name, signature, superName, interfaces)
    }
}
