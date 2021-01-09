package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.loadAgentConfig
import com.jonnyzzz.mplay.agent.generated.MPlayVersions
import java.io.File
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain

@Suppress("unused")
object MPlayAgentImpl {
    @JvmStatic
    fun premain(
        arguments: String?,
        instrumentation: Instrumentation
    ) {
        require(javaClass.classLoader == null) {
            "this class should be loaded with bootstrap classloader, but was " + javaClass.classLoader
        }

        println("Starting MPlay Agent ${MPlayVersions.buildNumber}")

        val args: Map<String, String?> = (arguments ?: "")
            .split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map {
                val kv = it.split("=", limit = 2)
                kv[0] to kv.getOrNull(1)
            }.toMap().toSortedMap()

        val agentConfigArg = "config"
        val agentConfigFile = args[agentConfigArg]?.let { File(it)}
            ?: error("Failed to read the $agentConfigArg=<file> parameter from agent configuration")

        println("Configuration from $agentConfigFile")

        val agentConfig = runCatching {
            loadAgentConfig(agentConfigFile.readBytes())
        }.getOrElse {
            throw Error("Failed to read or parse agent configuration from $agentConfigFile. ${it.message}", it)
        }

        println(buildString {
            append("Classes to record: ")
            agentConfig
                .classesToRecordEvents
                .map { it.classNameToIntercept.substringAfterLast(".") }
                .toSortedSet()
                .joinTo(this)
            appendLine()
        })

        val interceptor = buildClassInterceptor(agentConfig)
        instrumentation.addTransformer(object: ClassFileTransformer {
            override fun transform(
                loader: ClassLoader?,
                className: String,
                classBeingRedefined: Class<*>?,
                protectionDomain: ProtectionDomain?,
                classfileBuffer: ByteArray
            ): ByteArray? {
                return interceptor.intercept(className, classfileBuffer)
            }
        })
    }
}
