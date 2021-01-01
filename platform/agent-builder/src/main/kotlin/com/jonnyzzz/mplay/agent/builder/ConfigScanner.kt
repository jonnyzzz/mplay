package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.config.MPlayConfig
import org.reflections.Reflections
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ConfigurationClasspath(
    val configurationClasses: List<ConfigurationClass>
)

fun resolveConfigurationFromArgs(args: Array<String>): ConfigurationClasspath {
    val classpath = resolveAppClassFiles(args)
    val classloader = loadConfigurationClasses(classpath)
    val configurationClasses = resolveConfigurationClasses(classloader)

    return ConfigurationClasspath(
        configurationClasses = configurationClasses,
    )
}

private fun loadConfigurationClasses(classpath: List<Path>): URLClassLoader {
    class M
    val ourLoader = M::class.java.classLoader
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

private fun resolveConfigurationClasses(classloader: ClassLoader): List<ConfigurationClass> {
    @Suppress("UNCHECKED_CAST")
    val configClasses = Reflections(classloader)
        .getTypesAnnotatedWith(MPlayConfig::class.java, false)
        .filterNotNull()
        .toList()

    val result = mutableListOf<ConfigurationClass>()

    for (configClazz in configClasses) {
        result += ConfigurationClass.fromConfigClass(configClazz)
    }
    return result
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

    return allRoots
}
