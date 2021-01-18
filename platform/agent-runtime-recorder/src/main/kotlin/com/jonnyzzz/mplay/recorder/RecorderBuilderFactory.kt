package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.runtime.MPlayInstanceRecorderBuilder
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorderBuilderFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class RecorderBuilderFactoryImpl : MPlayRecorderBuilderFactory {
    private lateinit var agentConfig: AgentConfig
    private lateinit var rawAgentArgs: Map<String, String>

    override fun visitAgentConfig(config: AgentConfig) {
        super.visitAgentConfig(config)
        this.agentConfig = config
    }

    override fun visitAgentParameters(agentParams: Map<String, String>) {
        this.rawAgentArgs = agentParams.toSortedMap()
    }

    private val config = lazy {
        val reportDirKey = "record-dir"
        val reportDir = rawAgentArgs[reportDirKey]
            ?: error("Failed to get '$reportDirKey' parameter from MPlay Javaagent args")

        val reportDirPath = Paths.get(reportDir).toAbsolutePath()

        if (!Files.isDirectory(reportDirPath)) {
            Files.createDirectories(reportDirPath)
        }

        println("MPlay Recorder is set to use $reportDirPath for reports")

        val writer = PerThreadWriter(reportDirPath, ".mplay")
        Runtime.getRuntime().addShutdownHook(object: Thread("MPlay shutdown thread") {
            override fun run() {
                println("MPlay. Running shutdown hook")
                runCatching { writer.close() }
            }
        })

        val classpathFromParam = rawAgentArgs["config-classpath"]?.let { File(it).readText() }?.split("\n") ?: listOf()
        val classpath = (classpathFromParam + agentConfig.configClasspath)
            .mapNotNull {
                runCatching { File(it).toURI().toURL() }.getOrNull()
            }.toTypedArray()
        val classloaders = RecorderConfigLoader(classpath)

        classloaders to writer
    }

    override fun newRecorderBuilderFactory(): MPlayInstanceRecorderBuilder {
        val (classloaders, writer) = config.value
        return InstanceRecorderBuilder(agentConfig, classloaders, writer)
    }
}
