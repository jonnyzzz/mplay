@file:Suppress("NonAsciiCharacters")

package com.jonnyzzz.mplay.agent

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KFunction

inline fun <reified Y> method(ƒ: KFunction<*>) = method(Y::class.java, ƒ.name)
fun method(clazz: Class<*>, name: String): MethodCallInfo {
    val candidates = clazz.methods
        .filter { it.name == name }
        .filter { !Modifier.isStatic(it.modifiers) && Modifier.isPublic(it.modifiers) }

    require(candidates.size == 1) {
        "Failed to find instance public method $name in ${clazz.name}: " + candidates
    }

    val (opcode, isInterface) = when {
        clazz.isInterface -> Opcodes.INVOKEINTERFACE to true
        else -> Opcodes.INVOKEVIRTUAL to false
    }

    return MethodCallInfo(clazz, candidates.single(), opcode, isInterface)
}

inline fun <reified Y> staticMethod(ƒ: KFunction<*>) = staticMethod(Y::class.java, ƒ.name)
fun staticMethod(clazz: Class<*>, name: String): MethodCallInfo {
    val candidates = clazz.methods
        .filter { it.name == name }
        .filter { Modifier.isStatic(it.modifiers) && Modifier.isPublic(it.modifiers) }

    require(candidates.size == 1) { "Failed to find static public method $name in ${clazz.name}: " + candidates }
    return MethodCallInfo(clazz, candidates.single(), Opcodes.INVOKESTATIC, false)
}

class MethodCallInfo(
    private val owner: Class<*>,
    private val method: Method,
    private val opcode: Int,
    private val isInterface: Boolean
) {
    val returnType: Class<*> get() = method.returnType

    fun accept(mv: MethodVisitor) {
        mv.visitMethodInsn(
            opcode,
            Type.getInternalName(owner),
            method.name,
            Type.getMethodDescriptor(method),
            isInterface
        )
    }

    override fun toString() = "${owner.simpleName}#${method.name} ${Type.getMethodDescriptor(method)}"
}

fun MethodVisitor.visitMethodInsn(e: MethodCallInfo) {
    e.accept(this)
}
