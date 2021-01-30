package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.generated.MPlayVersions
import com.jonnyzzz.mplay.agent.runtime.MPlayInstanceRecorderBuilder
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorderBuilderFactory
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorderFactory
import java.io.File
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.net.*
import java.security.ProtectionDomain
import java.util.*
import java.util.jar.JarFile

@Suppress("unused")
object MPlayAgentImpl {
    @JvmStatic
    fun premain(
        arguments: String?,
        instrumentation: Instrumentation,
        agentJar: JarFile,
    ) {
        require(javaClass.classLoader == null) {
            "this class should be loaded with bootstrap classloader, but was " + javaClass.classLoader
        }

        println("Starting MPlay Agent ${MPlayVersions.buildNumber}")

        val agentConfig = resolveAgentConfig(arguments, agentJar)

        val factory = object: MPlayRecorderBuilderFactory {
            val recorderClassLoader = lazy {
                val recorderClasspath = agentConfig.recorderClasspath.map { File(it).toURI().toURL() }.toTypedArray()
                URLClassLoader(recorderClasspath, null)
            }

            private val factory by lazy {
                val factories = ServiceLoader
                    .load(MPlayRecorderBuilderFactory::class.java, recorderClassLoader.value)
                    .toList()

                if (factories.size != 1 ) {
                    error("There are none or several implementations found for "
                            + "${MPlayRecorderBuilderFactory::class.java.simpleName} in the classpath:"
                            + agentConfig.recorderClasspath.toSortedSet().joinToString("") { "\n  $it" }
                            + "\n implementation classes:"
                            + factories.map { it.javaClass.name }.sorted().joinToString("") { "\n  $it" }
                            + "\n please check configuration of the MPlay agent"
                    )
                }

                val factory = factories.single()
                factory.visitAgentConfig(agentConfig)
                factory.visitAgentParameters(agentConfig.recorderParams)
                factory
            }

            override fun newRecorderBuilderFactory(): MPlayInstanceRecorderBuilder = factory.newRecorderBuilderFactory()
        }

        MPlayRecorderFactory.factory = factory

        instrumentation.addTransformer(object : ClassFileTransformer {
            private val interceptor = buildClassInterceptor(agentConfig)

            override fun transform(
                loader: ClassLoader?,
                className: String,
                classBeingRedefined: Class<*>?,
                protectionDomain: ProtectionDomain?,
                classfileBuffer: ByteArray
            ): ByteArray? {
                if (loader == null) {
                    //do not deal with bootstrap classloader
                    return null
                }

                if (factory.recorderClassLoader.isInitialized() && factory.recorderClassLoader.value === loader) {
                    //make sure we are not processing our own types
                    return null
                }

                val classFqn = className.replace("/", ".")
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
