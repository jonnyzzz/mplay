package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.runtime.MPlayConstructorRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayInstanceRecorderBuilder

class InstanceRecorderBuilder(
    private val agentConfig: AgentConfig,
    private val classloaders: RecorderConfigLoader,
    private val perThreadWriter: PerThreadWriter,
) : MPlayInstanceRecorderBuilder {
    private var recordingClassName: String? = null
    private var configurationClassName: String? = null

    override fun visitRecordingClassName(recordingClassName: String) {
        this.recordingClassName = recordingClassName
    }

    override fun visitConfigurationClassName(configurationClassName: String) {
        this.configurationClassName = configurationClassName
    }

    override fun newConstructorRecorder(): MPlayConstructorRecorder {
        val interceptClassTask = agentConfig.classesToRecordEvents
            .singleOrNull { it.classNameToIntercept == recordingClassName && it.configClassName == configurationClassName}
            ?: return NopRecorder

        return ConstructorRecorder(
            interceptClassTask = interceptClassTask,
            classloaders = classloaders,
            perThreadWriter = perThreadWriter,
        )
    }
}
