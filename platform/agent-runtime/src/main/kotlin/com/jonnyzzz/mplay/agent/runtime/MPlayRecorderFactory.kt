package com.jonnyzzz.mplay.agent.runtime

/**
 * Entry point for the method recording in the generated classes
 */
object MPlayRecorderFactory {
    lateinit var factory: MPlayRecorderBuilderFactory

    /**
     * This method is executed in bytecode
     */
    @JvmStatic
    fun newRecorderBuilder(): MPlayInstanceRecorderBuilder {
        return factory.newRecorderBuilderFactory()
    }
}
