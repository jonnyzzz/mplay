package com.jonnyzzz.mplay.agent.runtime

import com.jonnyzzz.mplay.agent.config.AgentConfig

/**
 * The [ServiceLoader] interface of the actual implementation
 * of the MPlay recorder.
 *
 * To provide an extension, implement this interface,
 * place it's name into the
 * `META-INF/services/com.jonnyzzz.mplay.agent.runtime.MPlayRecorderBuilderFactory`
 * file name and provide the JAR into the javaagent config
 */
interface MPlayRecorderBuilderFactory {

    /**
     * Notifies the implementation on the additional
     * configuration options that are available for this run
     * inside an application with the MPlay Agent
     */
    fun setConfig(rawAgentArgs: Map<String, String>,
                  config: AgentConfig) = Unit

    fun newRecorderBuilderFactory() : MPlayRecorderBuilder
}
