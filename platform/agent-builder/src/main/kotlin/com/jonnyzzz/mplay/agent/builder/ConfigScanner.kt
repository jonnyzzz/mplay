package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.config.MPlayConfig
import org.objectweb.asm.*
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

fun resolveConfiguration(args: Array<String>) {
    for (clazz in resolveAppClassFiles(args)) {
        readClass(clazz)
    }
}

private fun resolveAppClassFiles(args: Array<String>): List<Path> {
    val classpathParam = args
        .param("classpath")
        .flatMap { it.split(File.pathSeparator) }

    val classpathFromFile = args
        .param("classpathFile")
        .mapNotNull { runCatching { File(it).readText() }.getOrNull() }
        .flatMap { it.splitToSequence("\n").mapNotNull { it.trimAndNullIfBlank() } }

    val allRoots = (classpathParam + classpathFromFile)
        .distinct()
        .map { Paths.get(it) }
        .filter { Files.exists(it) }

    println("Resolved roots:" + allRoots.joinToString("") { "\n  $it" } + "\n")

    val classFiles = allRoots
        .flatMap {
            when {
                Files.isRegularFile(it) && it.fileName.toString().endsWith(".jar") ->
                    FileSystems.newFileSystem(it,null).rootDirectories.toList()

                Files.isDirectory(it) ->
                    listOf(it)

                else -> emptyList()
            }
        }
        .filter { Files.isDirectory(it) }
        .flatMap {
            Files
                .walk(it)
                .filter { clazz ->
                    Files.isRegularFile(clazz) && clazz.fileName.toString().endsWith(".class")
                }.toList()
        }

    println("Collected ${classFiles.size} setup class files")

    return classFiles
}


private fun readClass(clazz: Path) {
    val reader = ClassReader(Files.readAllBytes(clazz))

    var hasAnnotation = false
    lateinit var classNameInfo : String

    val visitor = object : ClassVisitor(Opcodes.ASM9) {
        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            classNameInfo = "$name @ $signature"
            super.visit(version, access, name, signature, superName, interfaces)
        }

        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
            hasAnnotation = hasAnnotation or (descriptor == Type.getType(MPlayConfig::class.java).descriptor)
            return super.visitAnnotation(descriptor, visible)
        }
    }

    reader.accept(visitor, ClassReader.SKIP_CODE)

    if (hasAnnotation) {
        println("Found config class: $classNameInfo")
    }
}
