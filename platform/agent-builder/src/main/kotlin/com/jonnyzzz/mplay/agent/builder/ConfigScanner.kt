package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.config.MPlayConfig
import com.jonnyzzz.mplay.config.MPlayConfiguration
import org.objectweb.asm.*
import org.reflections.Reflections
import java.io.File
import java.lang.reflect.ParameterizedType
import java.net.URLClassLoader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

class ConfigurationClasspath(
    val classpath: List<Path>,
) {
    val classloader by lazy  {
        URLClassLoader(classpath.mapNotNull { runCatching { it.toUri().toURL() }.getOrNull() }.toTypedArray(), null)
    }

    val configurationClasses: List<ConfigurationClass<*>> by lazy {
        val annotationType = classloader.loadClass(MPlayConfig::class.java.name)
        val configType = classloader.loadClass(MPlayConfiguration::class.java.name)

        @Suppress("UNCHECKED_CAST")
        val configClasses = Reflections(classloader)
            .getTypesAnnotatedWith(annotationType as Class<out Annotation>, false)
            .filterNotNull()
            .toList()

        configClasses.map { configClazz ->
            if (!configType.isAssignableFrom(configClazz)) {
                error("The MPlay configuration ${configClazz.name} must " +
                        "directly implement ${MPlayConfiguration::class.java.name}")
            }

            val interceptType = configClazz.genericInterfaces.mapNotNull {
                if (it !is ParameterizedType) return@mapNotNull null
                val rawType = it.rawType as? Class<*> ?: return@mapNotNull null
                if (rawType != configType) return@mapNotNull null
                it.actualTypeArguments.single()
            }.singleOrNull() ?: error("The MPlay configuration ${configClazz.name} must " +
                    "directly implement ${MPlayConfiguration::class.java.name}")

            @Suppress("UNCHECKED_CAST")
            ConfigurationClass(
                configClass = configClazz as Class<out MPlayConfiguration<Any>>,
                interceptType = interceptType
            )
        }
    }
}

data class ConfigurationClass<T>(
    val configClass: Class<out MPlayConfiguration<T>>,

    /**
     * The class that we use to record (and later play) the
     * method calls (aka events)
     */
    val interceptType: java.lang.reflect.Type
)

fun resolveConfiguration(args: Array<String>) = ConfigurationClasspath(
    classpath = resolveAppClassFiles(args),
)

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

    return allRoots
}

private fun listAllClasses(classpath: List<Path>) : List<Path> {
    val classFiles = classpath
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


private fun readClass(clazz: Path) : String? {
    val reader = ClassReader(Files.readAllBytes(clazz))

    var hasAnnotation = false
    lateinit var configurationClassName : String

    val visitor = object : ClassVisitor(Opcodes.ASM9) {
        override fun visit(
            version: Int,
            access: Int,
            name: String,
            signature: String?,
            superName: String?,
            interfaces: Array<out String>?
        ) {
            configurationClassName = name
            super.visit(version, access, name, signature, superName, interfaces)
        }

        override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
            hasAnnotation = hasAnnotation or (descriptor == Type.getType(MPlayConfig::class.java).descriptor)
            return super.visitAnnotation(descriptor, visible)
        }
    }

    reader.accept(visitor, ClassReader.SKIP_CODE)
    if (!hasAnnotation) return null

    println("Found config class: $configurationClassName")
    return configurationClassName
}
