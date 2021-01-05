@file:Suppress("unused", "ProtectedInFinal", "UNUSED_PARAMETER")

package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.builder.ConfigurationClass
import com.jonnyzzz.mplay.agent.builder.fromClass
import com.jonnyzzz.mplay.agent.builder.toAgentConfig
import com.jonnyzzz.mplay.agent.builder.toClasspath
import org.junit.Test
import java.util.function.Consumer

class AgentIntegrationTest {
    @Test
    fun testInterceptSimpleClass() {
        class TestClass {
            private fun method(x: Long): Int = error("")
            protected fun method(x: Int): Int = error("")
            fun method() {
                println("Calling the Method of TestClass. ${javaClass.classLoader}")
            }
        }

        doInterceptTest<TestClass> {
            method()
        }
    }

    @Test
    fun testInterceptSimpleClassWithLongReturnValue() {
        class TestClass {
            private fun method(x: Long): Int = error("")
            protected fun method(x: Int): Int = error("")
            fun method(d: Double): Long {
                println("Calling the Method of TestClass. ${javaClass.classLoader}")
                return (d * 12345).toLong()
            }
        }

        doInterceptTest<TestClass> {
            method(123.0)
        }
    }

    @Test
    fun testInterceptSimpleClassWithThrow() {
        class TestClass {
            private fun method(x: Long): Int = error("")
            protected fun method(x: Int): Int = error("")
            fun method() {
                println("Calling the Method of TestClass. ${javaClass.classLoader}")
                throw RuntimeException("this is test")
            }
        }

        doInterceptTest<TestClass> {
            try {
                method()
            } catch (t: RuntimeException) {
                if (t.message != "this is test") throw t
            }
        }
    }

    @Test
    fun testInterceptClassWithStaticConstructor() {
        doInterceptTest<TestClassWithStaticInit> {
            method()
        }
    }

    @Test
    fun testInterceptClassWithFinalArg() {
        doInterceptTest<TestClassWithFinalArg> {
            method(5)
        }
    }

    @Test
    fun testInterceptGenericClass() {
        class TestClass<R> {
            fun <Q> method(q: Q, p: Long): R? {
                println("Calling the Method of TestClass. ${javaClass.classLoader} $q $p")
                return null
            }
        }

        doInterceptTest<TestClass<*>> {
            method("42", 42L)
        }
    }
}


inline fun <reified T> doInterceptTest(crossinline testAction: T.() -> Unit) {
    //the trick is that there will be an actual class, so we could re-load it
    //from the different classloader
    val scope = Consumer<T> { t -> t.testAction() }

    val config = ConfigurationClass.fromClass<T>().toClasspath()
    val agentConfig = config.toAgentConfig()
    val interceptor = buildClassInterceptor(agentConfig)

    InstrumentingClassLoader(interceptor).apply {
        val testClazz = loadClassByName<T>()
        val testObj = testClazz.getConstructor().newInstance()

        @Suppress("UNCHECKED_CAST")
        val scopeCopy = loadClass(scope.javaClass.name).getConstructor().newInstance() as Consumer<Any?>
        scopeCopy.accept(testClazz.cast(testObj))
    }
}
