package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.builder.generated.MPlayVersions

object BuilderMain {
    @JvmStatic
    fun main(args: Array<String>) {
        println("MPlay Agent Builder ${MPlayVersions.buildNumber}")
        println()

        val agentJar = BuilderMain::class.java.getResourceAsStream("/mplay-agent/mplay-agent.jar")
            ?: error("Failed to resolve the agent stub jar from resources")

        resolveConfiguration(args)
    }
}
