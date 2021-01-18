package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.builder.loadClassBytes
import com.jonnyzzz.mplay.agent.builder.toMethodRef
import com.jonnyzzz.mplay.agent.config.MethodRef
import org.objectweb.asm.*
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

inline fun <reified T> primaryConstructors() = T::class.java.primaryConstructors()

fun Class<*>.primaryConstructors() = resolvePrimaryConstructors(this)

private fun resolvePrimaryConstructors(clazz: Class<*>) : List<Constructor<*>> {
    val allConstructors = clazz
        .declaredConstructors
        .filter { !Modifier.isStatic(it.modifiers) }
        .distinct()
        .associateWith { it.toMethodRef() }

    val reader = ClassReader(clazz.loadClassBytes())
    val resolver = PrimaryConstructorResolver()
    reader.accept(resolver, ClassReader.EXPAND_FRAMES)

    return allConstructors
        .filter { (_, ref) -> ref !in resolver.constructorsThatCallsThis }
        .map { it.key }
        .filterNotNull()
}

private class PrimaryConstructorResolver : ClassVisitor(Opcodes.ASM9) {
    private lateinit var classJvmName: String
    val constructorsThatCallsThis = mutableSetOf<MethodRef>()

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        classJvmName = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        if (name != "<init>") return null

        /// It is not trivial to detect if a constructor calls another
        /// constructor of that type. We use the [AdviceAdapter] logic
        /// that help to inject a code just after the constructor call at
        /// the bytecode level. The main trick is that we remember all
        /// INVOKESPECIAL instructions to later resolve if that was a
        /// another this constructor call or a base constructor call.
        ///
        /// In fact, it's not trivial otherwise, e.g. on may call another
        /// constructors from a constructor like
        /// ```
        ///   class C(y: C?) {
        ///     constructor() : this(C(null))
        ///   }
        /// ```
        /// see tests for more details
        return object : org.objectweb.asm.commons.AdviceAdapter(Opcodes.ASM9, null, access, name, descriptor) {
            private var isCallingThisConstructor = false

            override fun visitMethodInsn(
                opcode: Int,
                owner: String,
                name: String,
                descriptor: String,
                isInterface: Boolean
            ) {
                isCallingThisConstructor = !isInterface &&
                        opcode == Opcodes.INVOKESPECIAL &&
                        name == "<init>" && owner == classJvmName

                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }

            override fun onMethodEnter() {
                if (isCallingThisConstructor) {
                    constructorsThatCallsThis += MethodRef(name, descriptor)
                }
            }
        }
    }
}
