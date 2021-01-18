package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.agent.runtime.MPlayConstructorRecorder
import com.jonnyzzz.mplay.agent.runtime.MPlayInstanceRecorderBuilder

class InstanceRecorderBuilder(
    private val classloaders: RecorderConfigLoader,
    private val perThreadWriter: PerThreadWriter,
) : MPlayInstanceRecorderBuilder {
    private var recordingClassName: String? = null
    private var configurationClassName: String? = null

    override fun visitRecordingClassName(recordingClassName: String) {
        this.recordingClassName = recordingClassName
        super.visitRecordingClassName(recordingClassName)
    }

    override fun visitConfigurationClassName(configurationClassName: String) {
        this.configurationClassName = configurationClassName
        super.visitConfigurationClassName(configurationClassName)
    }

    override fun newConstructorRecorder(): MPlayConstructorRecorder {
        return ConstructorRecorder(
            classloaders = classloaders,
            perThreadWriter = perThreadWriter,
            recordingClassName = recordingClassName ?: error("recordingClassName is not set"),
            configClassName = configurationClassName,
        )
    }
}
