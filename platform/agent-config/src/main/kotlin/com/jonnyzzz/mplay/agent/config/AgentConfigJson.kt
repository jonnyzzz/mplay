package com.jonnyzzz.mplay.agent.config

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


fun saveAgentConfig(config: AgentConfig): ByteArray {
    return Json { prettyPrint = true }.encodeToString(config).toByteArray(Charsets.UTF_8)
}

fun loadAgentConfig(config: ByteArray) : AgentConfig {
    return Json.decodeFromString(config.toString(Charsets.UTF_8))
}

