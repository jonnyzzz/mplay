package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.runtime.*
import java.nio.file.Files
import java.nio.file.Paths

class RecorderBuilderFactoryImpl : MPlayRecorderBuilderFactory {
    private lateinit var agentConfig: AgentConfig
    private lateinit var rawAgentArgs: Map<String, String>
    private lateinit var targetReport: MethodCallsWriterPaths

    override fun setConfig(rawAgentArgs: Map<String, String>, config: AgentConfig) {
        this.agentConfig = config
        this.rawAgentArgs = rawAgentArgs.toSortedMap()

        val reportDirKey = "record-dir"
        val reportDir = rawAgentArgs[reportDirKey] ?: error("Failed to get '$reportDirKey' parameter from MPlay Javaagent args")
        val reportDirPath = Paths.get(reportDir).toAbsolutePath()

        if (!Files.isDirectory(reportDirPath)) {
            Files.createDirectories(reportDirPath)
        }

        targetReport = MethodCallsWriterPaths(reportDirPath)
        println("MPlay Recorder is set to use $targetReport for reports")
        super.setConfig(rawAgentArgs, config)
    }

    override fun newRecorderBuilderFactory(): RecorderBuilderImpl {
        return RecorderBuilderImpl()
    }
}

class RecorderBuilderImpl : MPlayRecorderBuilder {
    override fun visitConstructorParametersComplete(): RecorderImpl {
        return RecorderImpl()
    }
}

class RecorderImpl : MPlayRecorder {
    override fun onMethodEnter(methodName: String, jvmMethodDescriptor: String): MPlayMethodCallRecorder {
        return MethodCallRecorderImpl()
    }
}

class MethodCallRecorderImpl : MPlayMethodCallRecorder {
    override fun visitParametersComplete(): MethodResultRecorderImpl {
        return MethodResultRecorderImpl()
    }
}

class MethodResultRecorderImpl : MPlayMethodResultRecorder {
    override fun commit() {
    }
}
