package com.jonnyzzz.mplay.agent.runtime

import com.jonnyzzz.mplay.agent.runtime.impl.MPlayRecorderImpl
import java.io.File

/**
 * Entry point for the method recording in the generated classes
 */
object MPlayRecorderFactory {
    /**
     * This method is executed in bytecode
     */
    @JvmStatic
    fun newRecorderBuilder(
        recordingClass: Class<*>,
        recordingClassName: String,
        configClassName: String,
        /**
         * classpath separated with [File.separator]
         */
        configClasspath: String,
    ): MPlayRecorderBuilder {
        //TODO: make it use Service API to load the specific service
        //TODO: make sure we only capture events for the specified class, not it's subclasses (check options if needed)
        //we may do caching here if needed
        return MPlayRecorderImpl(
            recordingClassName,
            configClassName,
            configClasspath.split(File.separator).map { File(it).toURI().toURL() })
    }
}
