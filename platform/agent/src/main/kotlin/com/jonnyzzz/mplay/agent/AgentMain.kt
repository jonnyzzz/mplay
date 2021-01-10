package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.config.loadAgentConfig
import com.jonnyzzz.mplay.agent.generated.MPlayVersions
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorderBuilder
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorderBuilderFactory
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorderFactory
import java.io.File
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.net.*
import java.security.ProtectionDomain
import java.util.*

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

        val args: Map<String, String> = (arguments ?: "")
            .split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull {
                val kv = it.split("=", limit = 2)
                kv[0] to (kv.getOrNull(1) ?: return@mapNotNull null)
            }.toMap().toSortedMap()

        val agentConfigArg = "config"
        val agentConfigFile = args[agentConfigArg]?.let { File(it) }
            ?: error("Failed to read the $agentConfigArg=<file> parameter from agent configuration")

        println("Configuration from $agentConfigFile")

        val agentConfig = runCatching {
            loadAgentConfig(agentConfigFile.readBytes())
        }.getOrElse {
            throw Error("Failed to read or parse agent configuration from $agentConfigFile. ${it.message}", it)
        }

        println(buildString {
            appendLine("Classes to record: ")
            agentConfig
                .classesToRecordEvents
                .map { it.classNameToIntercept }
                .toSortedSet()
                .joinTo(this, "") { "  $it" }
            appendLine()
        })

        val recorderClasspathKey = "recorder-classpath"
        val recorderClasspathFile = args[recorderClasspathKey]
            .ifNotNull { error("The '$recorderClasspathKey' parameter is missing in javaagent args") }
            .let { File(it) }

        val recorderClasspath = recorderClasspathFile
            .runCatching { readText() }
            .getOrElse { error("Failed to read $recorderClasspathFile. ${it.message}") }
            .split("\n")
            .mapNotNull { it.trim().takeIf { it.isNotBlank() } }
            .map { File(it) }
            .onEach { require(it.isFile) { "File $it must exist" } }
            .map { it.toURI().toURL()}

        println("MPlay Recorder Classpath URLs: " + recorderClasspath.joinToString(""){ "\n  $it"})

        MPlayRecorderFactory.factory = object: MPlayRecorderBuilderFactory {
            private val factory by lazy {
                val recorderClassLoader = URLClassLoader(recorderClasspath.toTypedArray(), null)
                val factory = ServiceLoader.load(MPlayRecorderBuilderFactory::class.java, recorderClassLoader)
                    .singleOrNull()
                    ?: error("There are several or nome implementations found for ${MPlayRecorderBuilderFactory::class.java.simpleName} ")

                factory.setConfig(args, agentConfig)
                factory
            }

            override fun newRecorderBuilderFactory(): MPlayRecorderBuilder = factory.newRecorderBuilderFactory()
        }

        instrumentation.addTransformer(object : ClassFileTransformer {
            private val interceptor = buildClassInterceptor(agentConfig)

            override fun transform(
                loader: ClassLoader?,
                className: String,
                classBeingRedefined: Class<*>?,
                protectionDomain: ProtectionDomain?,
                classfileBuffer: ByteArray
            ): ByteArray? {
                val classFqn = className.replace("/", ".")
                println("Checking $classFqn")
                val patched = interceptor.intercept(classFqn, classfileBuffer)
                if (patched != null) {
                    println("MPlay instrumented $classFqn")
                }
                return patched
            }
        })
    }
}

inline fun <T : Any> T?.ifNotNull(error: () -> Nothing): T = this ?: error()
