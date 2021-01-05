@file:Suppress("unused", "ProtectedInFinal", "UNUSED_PARAMETER")

package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.builder.*
import org.junit.Test
import java.lang.RuntimeException
import java.lang.reflect.InvocationTargetException

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
    fun testInterceptSimpleClassWithLongReturnValue() {
        class TestClass {
            private fun method(x: Long) : Int = error("")
            protected fun method(x: Int) : Int = error("")
            fun method(d: Double) : Long {
                println("Calling the Method of TestClass. ${javaClass.classLoader}")
                return (d * 12345).toLong()
            }
        }

        val config = ConfigurationClass.fromClass<TestClass>().toClasspath()
        val agentConfig = config.toAgentConfig()
        val interceptor = buildClassInterceptor(agentConfig)

        InstrumentingClassLoader(interceptor).apply {
            val testClazz = loadClassByName<TestClass>()
            val testObj = testClazz.getConstructor().newInstance()
            testClazz.getMethod((TestClass::method).name, Double::class.java).invoke(testObj, 123.0)
        }
    }

    @Test
    fun testInterceptSimpleClassWithThrow() {
        class TestClass {
            private fun method(x: Long) : Int = error("")
            protected fun method(x: Int) : Int = error("")
            fun method() {
                println("Calling the Method of TestClass. ${javaClass.classLoader}")
                throw RuntimeException("this is test")
            }
        }

        val config = ConfigurationClass.fromClass<TestClass>().toClasspath()
        val agentConfig = config.toAgentConfig()
        val interceptor = buildClassInterceptor(agentConfig)

        InstrumentingClassLoader(interceptor).apply {
            val testClazz = loadClassByName<TestClass>()
            val testObj = testClazz.getConstructor().newInstance()
            try {
                testClazz.getMethod((TestClass::method).name).invoke(testObj)
            } catch (t: InvocationTargetException) {
                if (t.targetException.message != "this is test") throw t.targetException
            }
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
    fun testInterceptClassWithFinalArg() {
        val config = ConfigurationClass.fromClass<TestClassWithFinalArg>().toClasspath()
        val agentConfig = config.toAgentConfig()

        val interceptor = buildClassInterceptor(agentConfig)

        InstrumentingClassLoader(interceptor).apply {
            val testClazz = loadClassByName<TestClassWithFinalArg>()
            val testObj = testClazz.getConstructor().newInstance()
            testClazz.getMethod("method", Byte::class.java).invoke(testObj, 5.toByte())
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

