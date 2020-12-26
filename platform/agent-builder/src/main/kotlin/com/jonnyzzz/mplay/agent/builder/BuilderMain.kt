package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.builder.generated.MPlayVersions
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.toList

object BuilderMain {
    @JvmStatic
    fun main(args: Array<String>) {
        println("MPlay Agent Builder ${MPlayVersions.buildNumber}")
        println()

        val classpathParam: List<String> = args
            .param("classpath")
            .flatMap { it.split(File.pathSeparator) }

        val classpathFromFile: List<String> = args
            .param("classpathFile")
            .mapNotNull { kotlin.runCatching { File(it).readText() }.getOrNull() }
            .flatMap { it.splitToSequence("\n").mapNotNull { it.trimAndNullIfBlank() } }

        val classFiles = (classpathParam + classpathFromFile)
            .map { Paths.get(it) }
            .flatMap {
                when {
                    Files.isRegularFile(it) && it.fileName.toString().endsWith(".jar") -> FileSystems.newFileSystem(it,null).rootDirectories.toList()
                    Files.isDirectory(it) -> listOf(it)
                    else -> emptyList()
                }
            }
            .filterNotNull()
            .flatMap {
                Files.walk(it).filter { it?.fileName?.toString()?.endsWith(".class") == true }.toList()
            }

        println("Collected ${classFiles.size} setup class files")

        val agentJar = BuilderMain::class.java.getResourceAsStream("/mplay-agent/mplay-agent.jar")
            ?: error("Failed to resolve the agent stub jar from resources")

    }
}

private fun Array<String>.param(key: String) = mapNotNull { it.substringOrNull("--$key=") }

private fun String.substringOrNull(prefix: String): String? {
    return if (this.startsWith(prefix)) {
        this.removePrefix(prefix)
    } else {
        null
    }
}

private fun String.trimAndNullIfBlank(): String? = this.trim().run { if (isNotEmpty()) this else null }
