package com.jonnyzzz.mplay.agent.runtime

import com.jonnyzzz.mplay.agent.config.AgentConfig
import java.io.File

/**
 * Entry point for the method recording in the generated classes
 */
object MPlayRecorderFactory {
    lateinit var agentConfig: AgentConfig
    lateinit var factory: MPlayRecorderBuilderFactory

    /**
     * This method is executed in bytecode
     */
    @JvmStatic
    fun newRecorderBuilder(): MPlayRecorderBuilder {
        return factory.newRecorderBuilderFactory()
    }
}
