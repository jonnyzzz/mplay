package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.config.MPlayConfig
import com.jonnyzzz.mplay.config.MPlayConfiguration
import org.objectweb.asm.*
import org.reflections.Reflections
import java.io.File
import java.net.URLClassLoader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

class ConfigurationClasspath(
    val classpath: List<Path>,
) {
    val classloader by lazy { loadConfigurationClasses() }
    val annotationType by lazy { classloader.loadClass(MPlayConfig::class.java.name) }
    val configType by lazy { classloader.loadClass(MPlayConfiguration::class.java.name) }
    val configurationClasses by lazy { resolveConfigurationClasses() }
}

private fun ConfigurationClasspath.loadConfigurationClasses(): URLClassLoader {
    val ourLoader = javaClass.classLoader
    val urls = classpath.mapNotNull { runCatching { it.toUri().toURL() }.getOrNull() }.toTypedArray()
    return object : URLClassLoader(urls, null) {
        private val packagePrefix = MPlayConfig::class.java.packageName + "."

        override fun loadClass(name: String, resolve: Boolean): Class<*> {
            //we make sure we load config classes from our classpath
            if (name.startsWith(packagePrefix)) {
                return ourLoader.loadClass(name)
            }
            return super.loadClass(name, resolve)
        }
    }
}

private fun ConfigurationClasspath.resolveConfigurationClasses(): List<ConfigurationClass> {
    @Suppress("UNCHECKED_CAST")
    val configClasses = Reflections(classloader)
        .getTypesAnnotatedWith(annotationType as Class<out Annotation>, false)
        .filterNotNull()
        .toList()

    val result = mutableListOf<ConfigurationClass>()

    for (configClazz in configClasses) {
        result += ConfigurationClass.fromConfigClass(this, configClazz)
    }
    return result
}

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
