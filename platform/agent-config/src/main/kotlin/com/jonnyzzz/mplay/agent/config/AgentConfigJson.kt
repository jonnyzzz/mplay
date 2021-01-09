package com.jonnyzzz.mplay.agent.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


fun saveAgentConfig(config: AgentConfig): ByteArray {
    val om = jacksonObjectMapper()
    return om.writerWithDefaultPrettyPrinter().writeValueAsBytes(config)
}

fun loadAgentConfig(config: ByteArray) : AgentConfig {
    val om = jacksonObjectMapper()
    return om.readValue(config, AgentConfig::class.java)
}

