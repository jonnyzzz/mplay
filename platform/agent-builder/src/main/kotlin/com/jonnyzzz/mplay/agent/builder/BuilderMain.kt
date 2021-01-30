package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.builder.generated.MPlayVersions
import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.config.saveAgentConfig
import java.io.File
import java.net.URI
import java.nio.file.*

object BuilderMain {
    @JvmStatic
    fun main(args: Array<String>) {
        println("MPlay Agent Builder ${MPlayVersions.buildNumber}")
        println()

        val classpathRoots = resolveAppClassFiles(args)

        val configuration = resolveConfigurationFromArgs(args, classpathRoots)
        logConfigurationInfo(configuration)

        val agentConfig = configuration.toAgentConfig(classpathRoots)
            .copy(recorderClasspath = args.param("agent-runtime"))
            .copy(recorderParams = args
                .filter { it.startsWith("--") }
                .map { it.removePrefix("--") }
                .mapNotNull { s ->
                    val kv = s
                        .split("=", limit = 2)
                        .takeIf { it.size == 2 } ?: return@mapNotNull null
                    kv[0] to kv[1]
                }.toMap()
            )

        args.param("agent-config").forEach {
            val output = File(it)
            output.parentFile.mkdirs()
            output.writeBytes(saveAgentConfig(agentConfig))
        }

        packAgent(args, agentConfig)
    }

    private fun packAgent(args: Array<String>, agentConfig: AgentConfig) {
        val agentJarTargetPath = args
            .param("agent-path")
            .map { Path.of(it).toAbsolutePath() }
            .singleOrNull()
            ?: return

        run {
            val agentJarSourcePath = args
                .param("agent-jar")
                .map { Path.of(it) }.singleOrNull()
                ?: error("Failed to resolve mplay agent jar")

            Files.createDirectories(agentJarTargetPath.parent)
            Files.copy(agentJarSourcePath, agentJarTargetPath, StandardCopyOption.REPLACE_EXISTING)
        }

        val jar = URI("jar", agentJarTargetPath.toUri().toString(), null)
        FileSystems.newFileSystem(jar, HashMap<String, Any?>()).use { fs ->
            val jsonFile = fs.getPath("com/jonnyzzz/mplay/user-config/agent-config.json")
            Files.createDirectories(jsonFile.parent)
            Files.write(jsonFile, saveAgentConfig(agentConfig))
        }

        val agentArg = "-javaagent:$agentJarTargetPath"

        println()
        println("Generated MPlay agent. Add the following lines to the JVM to enable capture:")
        println("")
        println("    $agentArg ")
        println("")
        println("NOTE. All parameters for the Java agent are embedded into it.")
        println("NOTE. This file is non-transferable to another machine")

        args.param("agent-args-file").map { File(it) }.forEach { argsFile ->
            argsFile.parentFile.mkdirs()
            argsFile.writeText(agentArg)
        }
    }

    private fun logConfigurationInfo(configuration: ConfigurationClasspath) {
        //TODO: use AgentConfig for everything, there is no need for extra representation
        println("Selected configuration classes:")

        for (config in configuration.configurationClasses) {
            println("  $config")
            println("    public methods:")
            for (m in config.methodsToIntercept) {
                println("      $m")
            }
            println("    parameter types:")
            for (p in config.methodParameterTypes) {
                println("      $p")
            }
            if (config.baseClassesToIntercept.isNotEmpty()) {
                println("    base classes to make methods non-final:")
                for (b in config.baseClassesToIntercept) {
                    println("      ${b.name}")
                }
            }
        }
    }
}
