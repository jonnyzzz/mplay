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
    val configClasspath: List<String>,

    val classesToRecordEvents: List<InterceptClassTask>,

    val classesToOpenMethods: List<OpenClassMethodsTask> = listOf(),
)


