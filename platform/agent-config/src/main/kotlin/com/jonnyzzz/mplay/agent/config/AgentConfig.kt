@file:Suppress("KDocUnresolvedReference")

package com.jonnyzzz.mplay.agent.config

import kotlinx.serialization.Serializable

/**
 * Is it fine to assume these classes are included into the agent? Or mayby in the app classpath?
 */
@Serializable
data class AgentConfig(
    /**
     * classpath of the configuration classes
     */
    val configClasspath: List<String> = listOf(),
    val classesToRecordEvents: List<InterceptClassTask> = listOf(),
    val classesToOpenMethods: List<OpenClassMethodsTask> = listOf(),

    /**
     * classpath of the methods recorder implementation
     * @see com.jonnyzzz.mplay.agent.runtime.MPlayRecorderBuilderFactory
     */
    val recorderClasspath: List<String> = listOf(),
    val recorderParams: Map<String, String> = mapOf(),
)
