package com.jonnyzzz.mplay.agent.runtime

import java.io.File
import java.net.URL


data class MPlayRecorder(
    val recordingClassName: String,
    val configClassName: String,
    val configClasspath: List<URL>,
) {
    init {
        println("MPlayRecorder[$recordingClassName] created")
    }

    fun onMethodEnter(name: String) {
        println("MPlayRecorder[$recordingClassName] on method: $name")
    }

    companion object {
        /**
         * This method is executed in bytecode
         */
        @JvmStatic
        fun getInstance(
            recordingClassName: String,
            configClassName: String,
            /**
             * classpath separated with [File.separator]
             */
            configClasspath: String,
        ): MPlayRecorder {
            //we may do caching here if needed
            return MPlayRecorder(
                recordingClassName,
                configClassName,
                configClasspath.split(File.separator).map { File(it).toURI().toURL() })
        }
    }
}
