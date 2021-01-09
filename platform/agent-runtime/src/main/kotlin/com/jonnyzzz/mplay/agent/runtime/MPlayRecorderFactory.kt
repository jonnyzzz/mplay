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
    fun newRecorderBuilder(
        //TODO: recorderClasspath: String,
        /**
         * classpath separated with [File.separator]
         */
        classpath: String,
    ): MPlayRecorderBuilder = factory.newRecorderBuilderFactory()
}
