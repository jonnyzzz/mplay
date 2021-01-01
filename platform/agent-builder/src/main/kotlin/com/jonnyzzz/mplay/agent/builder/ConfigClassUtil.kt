package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.config.AgentConfig
import com.jonnyzzz.mplay.agent.config.InterceptClassTask
import com.jonnyzzz.mplay.config.MPlayConfiguration


inline fun <reified T : MPlayConfiguration<*>> ConfigurationClass.Companion.fromConfigClass() =
    fromConfigClass(T::class.java)


fun ConfigurationClass.toClasspath(): ConfigurationClasspath = ConfigurationClasspath(listOf(this))


fun ConfigurationClasspath.toAgentConfig() : AgentConfig {
    return AgentConfig(classesToRecordEvents = this.configurationClasses.map {
        InterceptClassTask(name = it.interceptedRawType.name)
    })
}
