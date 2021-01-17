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
     * Passes the MPlay agent configuration to the factory.
     *
     * It is guaranteed, this method is executed only once
     */
    fun visitAgentConfig(config: AgentConfig) = Unit

    /**
     * Sends the MPLay Javaagent parameters as-is to the
     * implementation of the factory. We use `;` symbol to
     * decode parameters
     *
     * It is guaranteed, this method is executed only once
     */
    fun visitAgentParameters(agentParams: Map<String, String>) = Unit

    /**
     * The factory method to create an instance of the
     * recorder. The object can (but not required to) be
     * reused between all other objects
     */
    fun newRecorderBuilderFactory() : MPlayInstanceRecorderBuilder
}
