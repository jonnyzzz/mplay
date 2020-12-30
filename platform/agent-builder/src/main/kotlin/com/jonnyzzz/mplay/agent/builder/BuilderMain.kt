package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.builder.generated.MPlayVersions

object BuilderMain {
    @JvmStatic
    fun main(args: Array<String>) {
        println("MPlay Agent Builder ${MPlayVersions.buildNumber}")
        println()

        val agentJar = BuilderMain::class.java.getResourceAsStream("/mplay-agent/mplay-agent.jar")
            ?: error("Failed to resolve the agent stub jar from resources")

        val configuration = resolveConfiguration(args)
        println("Selected configuration classes:")

        for (config in configuration.configurationClasses) {
            println("  $config")
            println("    public methods: ${config.publicMethods.size}")
            for (m in config.publicMethods) {
                println("      $m")
            }

            println("    parameter types:")
            for (p in config.methodParameterTypes) {
                println("      $p")
            }
        }
    }
}
