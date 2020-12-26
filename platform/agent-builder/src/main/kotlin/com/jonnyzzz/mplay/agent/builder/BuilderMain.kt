package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.builder.generated.MPlayVersions
import java.io.File

object BuilderMain {
    @JvmStatic
    fun main(args: Array<String>) {
        println("MPlay Agent Builder ${MPlayVersions.buildNumber}")

        val classpath = args
            .toList()
            .mapNotNull { it.substringOrNull("--classpath=") }
            .flatMap { it.split(File.separator) }

        val agentJar = BuilderMain::class.java.getResourceAsStream("/mplay-agent/mplay-agent.jar")
            ?: error("Failed to resolve the agent stub jar from resources")

    }
}


private fun String.substringOrNull(prefix: String): String? {
    return if (this.startsWith(prefix)) {
        this.removePrefix(prefix)
    } else {
        null
    }
}

