package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.config.loadAgentConfig
import java.io.File
import java.util.jar.JarFile

fun resolveAgentConfig(
    arguments: String?,
    agentJar: JarFile
): AgentConfig {
    val args = parseAgentArgs(arguments)
    var config = resolveAgentConfigImpl(args, agentJar)

    config = config
        .copy(recorderClasspath = resolveRecorderClasspath(args, config))
        .copy(recorderParams = (config.recorderParams + args).toSortedMap())

    logAgentConfig(config)
    return config
}

private fun parseAgentArgs(arguments: String?) : Map<String, String> = (arguments ?: "")
    .split(";")
    .map { it.trim() }
    .filter { it.isNotBlank() }
    .mapNotNull {
        val kv = it.split("=", limit = 2)
        kv[0] to (kv.getOrNull(1) ?: return@mapNotNull null)
    }.toMap().toSortedMap()


private fun resolveRecorderClasspath(
    args: Map<String, String>,
    agentConfig: AgentConfig,
): List<String> {
    val implicitClasspath = agentConfig.recorderClasspath

    val explicitClasspath = run {
        val recorderClasspathKey = "recorder-classpath"
        val recorderClasspathFile = args[recorderClasspathKey]
            .ifNotNull { return@run listOf<String>() }
            .let { File(it) }

        recorderClasspathFile
            .runCatching { readText() }
            .getOrElse { error("Failed to read $recorderClasspathFile. ${it.message}") }
            .split("\n")
    }

    val recorderClasspath = (implicitClasspath + explicitClasspath)
        .mapNotNull { it.trim().takeIf { it.isNotBlank() } }
        .distinct()
        .map { File(it) }
        .onEach { require(it.isFile) { "File $it must exist" } }

    println("MPlay Recorder Runtime Classpath URLs: " + recorderClasspath.joinToString(""){ "\n  $it"} + "\n")
    return recorderClasspath.map { it.path }
}

private fun resolveAgentConfigImpl(
    args: Map<String, String>,
    agentJar: JarFile
): AgentConfig {
    val agentConfigArg = "config"
    val agentConfigFile = args[agentConfigArg]?.let { File(it) }

    if (agentConfigFile == null) {
        agentJar.getJarEntry("com/jonnyzzz/mplay/user-config/agent-config.json")?.let { entry ->
            println("Loading embedded configuration...")
            try {
                val bytes = agentJar.getInputStream(entry).readAllBytes()
                return loadAgentConfig(bytes)
            } catch (t: Throwable) {
                throw Error("Failed to load embedded agent configuration. ${t.message}", t)
            }
        }
    }

    if (agentConfigFile == null) {
        error("Failed to read the $agentConfigArg=<file> parameter from agent configuration")
    }

    println("Configuration from $agentConfigFile")
    try {
        return loadAgentConfig(agentConfigFile.readBytes())
    } catch (t: Throwable) {
        throw Error("Failed to read or parse agent configuration from $agentConfigFile. ${t.message}", t)
    }
}

private fun logAgentConfig(agentConfig: AgentConfig) {
    println(buildString {
        append("Classes to record: ")
        agentConfig
            .classesToRecordEvents
            .map { it.classNameToIntercept }
            .toSortedSet()
            .joinTo(this, "") { "\n  $it" }
        appendLine()

        append("Classes to open methods: ")
        agentConfig
            .classesToOpenMethods
            .map { it.classNameToIntercept }
            .toSortedSet()
            .joinTo(this, "") { "\n  $it" }
        appendLine()
    })
}
