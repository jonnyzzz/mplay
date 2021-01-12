package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorderBuilderFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class RecorderBuilderFactoryImpl : MPlayRecorderBuilderFactory {
    private lateinit var agentConfig: AgentConfig
    private lateinit var rawAgentArgs: Map<String, String>
    private lateinit var targetReport: MethodCallsWriterPaths
    private lateinit var classloaders: RecorderConfigLoader

    override fun setConfig(rawAgentArgs: Map<String, String>, config: AgentConfig) {
        this.agentConfig = config
        this.rawAgentArgs = rawAgentArgs.toSortedMap()

        val reportDirKey = "record-dir"
        val reportDir = rawAgentArgs[reportDirKey]
            ?: error("Failed to get '$reportDirKey' parameter from MPlay Javaagent args")

        val reportDirPath = Paths.get(reportDir).toAbsolutePath()

        if (!Files.isDirectory(reportDirPath)) {
            Files.createDirectories(reportDirPath)
        }

        targetReport = MethodCallsWriterPaths(reportDirPath)
        println("MPlay Recorder is set to use $targetReport for reports")

        val classpathFromParam = rawAgentArgs["config-classpath"]?.let { File(it).readText() }?.split("\n") ?: listOf()
        val classpath = (classpathFromParam + agentConfig.configClasspath)
            .mapNotNull {
                runCatching { File(it).toURI().toURL() }.getOrNull()
            }.toTypedArray()
        classloaders = RecorderConfigLoader(classpath)

        super.setConfig(rawAgentArgs, config)
    }

    override fun newRecorderBuilderFactory() = RecorderBuilderImpl(classloaders)
}
