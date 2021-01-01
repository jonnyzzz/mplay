package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.builder.ConfigurationClass
import com.jonnyzzz.mplay.agent.builder.fromConfigClass
import com.jonnyzzz.mplay.agent.builder.toAgentConfig
import com.jonnyzzz.mplay.agent.builder.toClasspath
import com.jonnyzzz.mplay.config.MPlayConfiguration
import org.junit.Test

class AgentIntegrationTest {
    @Test
    fun testInterceptOpenClass() {
        class TestClass {
            fun method() {
                println("Calling the Method of TestClass. ${javaClass.classLoader}")
            }
        }

        class Config : MPlayConfiguration<TestClass>
        val config = ConfigurationClass.fromConfigClass<Config>().toClasspath()

        val agentConfig = config.toAgentConfig()

        val interceptor = buildClassInterceptor(agentConfig)

        InstrumentingClassLoader(interceptor).apply {
            loadClassByName<Config>().getConstructor().newInstance()

            val testClazz = loadClassByName<TestClass>()
            val testObj = testClazz.getConstructor().newInstance()
            testClazz.getMethod((TestClass::method).name).invoke(testObj)
        }
    }
}

private class InstrumentingClassLoader(
    private val interceptor: ClassInterceptor,
    private val realLoader: ClassLoader = InstrumentingClassLoader::class.java.classLoader,
) : ClassLoader(null) {

    override fun findClass(name: String): Class<*> {
        if (name.startsWith("kotlin.")) {
            return realLoader.loadClass(name)
        }

        val resource = name.replace('.', '/') + ".class"
        val resourceStream = realLoader.getResourceAsStream(resource)
            ?: throw ClassNotFoundException("Failed to find class $name at $resource")

        val originalBytes = resourceStream.use { it.readBytes() }
        val classBytes = interceptor.intercept(
            name,
            originalBytes,
        ) ?: originalBytes

        if (originalBytes.contentEquals(classBytes)) {
            println("Loading original class $name")
        } else {
            println("Loading patched class $name")
        }

        return defineClass(name, classBytes, 0, classBytes.size, null)
    }
}

private inline fun <reified Y> ClassLoader.loadClassByName(): Class<*> {
    val clazz = loadClass(Y::class.java.name)
    require(clazz !== Y::class.java) { "Loaded class $clazz has the same classloader as ${Y::class.java}!"}
    return clazz
}