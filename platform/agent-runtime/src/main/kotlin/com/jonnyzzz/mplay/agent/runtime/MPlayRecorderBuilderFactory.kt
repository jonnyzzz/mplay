package com.jonnyzzz.mplay.agent.runtime

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
    fun newRecorderBuilderFactory() : MPlayRecorderBuilder
}
