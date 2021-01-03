@file:Suppress("unused", "ProtectedInFinal", "UNUSED_PARAMETER")

package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.builder.*
import org.junit.Test

class AgentIntegrationTest {
    @Test
    fun testInterceptSimpleClass() {
        class TestClass {
            private fun method(x: Long) : Int = error("")
            protected fun method(x: Int) : Int = error("")
            fun method() {
                println("Calling the Method of TestClass. ${javaClass.classLoader}")
            }
        }

        val config = ConfigurationClass.fromClass<TestClass>().toClasspath()
        val agentConfig = config.toAgentConfig()
        val interceptor = buildClassInterceptor(agentConfig)

        InstrumentingClassLoader(interceptor).apply {
            val testClazz = loadClassByName<TestClass>()
            val testObj = testClazz.getConstructor().newInstance()
            testClazz.getMethod((TestClass::method).name).invoke(testObj)
        }
    }

    @Test
    fun testInterceptClassWithStaticConstructor() {
        val config = ConfigurationClass.fromClass<TestClassWithStaticInit>().toClasspath()
        val agentConfig = config.toAgentConfig()

        val interceptor = buildClassInterceptor(agentConfig)

        InstrumentingClassLoader(interceptor).apply {
            val testClazz = loadClassByName<TestClassWithStaticInit>()
            val testObj = testClazz.getConstructor().newInstance()
            testClazz.getMethod("method").invoke(testObj)
        }
    }

    @Test
    fun testInterceptGenericClass() {
        class TestClass<R> {
            fun <Q> method(q: Q, p: Long) : R? {
                println("Calling the Method of TestClass. ${javaClass.classLoader} $q $p")
                return null
            }
        }

        val config = ConfigurationClass.fromClass<TestClass<*>>().toClasspath()
        val agentConfig = config.toAgentConfig()
        val interceptor = buildClassInterceptor(agentConfig)

        InstrumentingClassLoader(interceptor).apply {
            val testClazz = loadClassByName<TestClass<*>>()
            val testObj = testClazz.getConstructor().newInstance()
            testClazz.getMethod("method", Any::class.java, Long::class.java).invoke(testObj, "42", 42L)
        }
    }
}

