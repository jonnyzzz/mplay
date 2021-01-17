package com.jonnyzzz.mplay.agent.runtime

import com.jonnyzzz.mplay.agent.config.AgentConfig

/**
 * The [java.util.ServiceLoader] interface of the actual implementation
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

    /**
     * The factory method to create an instance of the
     * recorder. The object can (but not required to) be
     * reused between all other objects
     */
    fun newRecorderBuilderFactory() : MPlayRecorderBuilder
}
