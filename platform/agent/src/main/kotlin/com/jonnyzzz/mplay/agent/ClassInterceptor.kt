package com.jonnyzzz.mplay.agent

import org.objectweb.asm.*

class PrimaryConstructorResolver : ClassVisitor(Opcodes.ASM9) {

    fun visit(reader: MPlayClass) {
        reader.newReader().accept(this, 0)
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        println("clazz: $name - $signature -- $superName -- ${interfaces.contentToString()}")
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        if (name != "<init>") return null

        println("  $name -- $descriptor -- $signature")
        return CollectAllInitMethods()
    }

    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        println("  - annotation: $descriptor $visible")
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitTypeAnnotation(
        typeRef: Int,
        typePath: TypePath?,
        descriptor: String?,
        visible: Boolean
    ): AnnotationVisitor? {
        println("  - typeAnnotation: $typeRef $typePath $descriptor $visible")
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible)
    }

    override fun visitAttribute(attribute: Attribute?) {
        println("  - attribute: $attribute")
        super.visitAttribute(attribute)
    }

}

private class CollectAllInitMethods : MethodVisitor(Opcodes.ASM9) {
    val owners = mutableListOf<String>()
    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        if (isInterface || opcode != Opcodes.INVOKESPECIAL || name != "<init>") return
        owners += owner
        println("method insn $owner -- $name -- $descriptor")
    }
}
