@file:Suppress("unused", "ProtectedInFinal", "UNUSED_PARAMETER")

package com.jonnyzzz.mplay.agent

import com.jonnyzzz.mplay.agent.builder.ConfigurationClass
import com.jonnyzzz.mplay.agent.builder.fromClass
import com.jonnyzzz.mplay.agent.builder.toAgentConfig
import com.jonnyzzz.mplay.agent.builder.toClasspath
import com.jonnyzzz.mplay.agent.runtime.MPlayRecorderFactory
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
    fun testCheckcastCouldBeNeeded() {
        class TestClass(x: String, arr: Array<Long>) {
            init {
                "$x + $arr".hashCode().toString()
            }

            fun method(x: String, arr: Array<Long>): Long = "this is $x and ${arr.joinToString()}".hashCode().toLong()
        }

        doInterceptTest<TestClass>("123", arrayOf(42L)) {
            method("123", arrayOf(42L))
        }
    }

    @Test
    fun testStackSizeLongReturn() {
        class TestClass(x: String, arr: Array<Long>) {
            fun method(x: String, arr: Array<Long>): Long = 1
        }

        doInterceptTest<TestClass>("123", arrayOf(42L)) {
            method("123", arrayOf(42L))
        }
    }

    @Test
    fun testInterceptBaseMethods() {
        open class PreBase {
            open fun preB() = 23
        }
        open class Base : PreBase() {
            fun baseMethod() {
                println("Calling the baseMethod of TestClass. ${javaClass.classLoader}")
            }
        }

        open class Inner : Base()

        class TestClass : Inner() {
            fun method() {
                println("Calling the method of TestClass. ${javaClass.classLoader}")
            }

            override fun preB(): Int {
                println("")
                return super.preB()
            }
        }

        doInterceptTest<TestClass> {
            baseMethod()
        }
    }

    @Test
    fun testDefaultMethodInInterface() {
        doInterceptTest<ClassWithDefaultMethod> {
            `fun`(Any())
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

    @Test
    fun testClassWithConditionsAndByteCodeFrames() {
        class TestClass {
            @Suppress("ConvertSecondaryConstructorToPrimary")
            constructor(x: Int) {
                if (x == 0) return
                if (x > 0) {
                    println("<INIT> : $x")
                }
                if ( x == 0) {
                    var y = 0
                    @Suppress("ForEachParameterNotUsed")
                    (0..x).forEach { y--
                        y++
                        if (y < 53) y++
                    }
                } else {
                    var y = 0
                    @Suppress("ForEachParameterNotUsed")
                    (0..x).forEach {
                        y++
                        if (y < 53) y--
                    }
                }
            }

            fun method(p: Long) {
                println("Calling the Method of TestClass. ${javaClass.classLoader} $p")
                if (p > 0) {
                    method(-p)
                }

                if (p < 0) {
                    method(0)
                }

                var z = 0
                @Suppress("ForEachParameterNotUsed")
                (0..p).forEach {
                    z++
                    if (z < 53) z--
                }
            }
        }

        doInterceptTest<TestClass>(42) {
            method(42L)
        }
    }

    //TODO: test bridge/synthetic methods are not included (e.g. ones from Generic specialization)
    //TODO: handle default methods implementations (at least we may warn that)
}


inline fun <reified T> doInterceptTest(vararg constructorArgs: Any,
                                       crossinline testAction: T.() -> Unit) {
    //the trick is that there will be an actual class, so we could re-load it
    //from the different classloader
    val scope = Consumer<T> { t -> t.testAction() }

    val config = ConfigurationClass.fromClass<T>().toClasspath()
    val agentConfig = config.toAgentConfig()
    val interceptor = buildClassInterceptor(agentConfig)

    MPlayRecorderFactory.factory = MPlayRecorderBuilderFactoryImpl()

    InstrumentingClassLoader(interceptor).apply {
        val testClazz = loadClass(T::class.java.name)
        require(testClazz !== T::class.java) {
            "Loaded class $testClazz has the same classloader as ${T::class.java}!"
        }

        val testObj = testClazz.constructors.single {
            it.parameterCount == constructorArgs.count()
        }.newInstance(*constructorArgs)

        @Suppress("UNCHECKED_CAST")
        val scopeCopy = loadClass(scope.javaClass.name).getConstructor().newInstance() as Consumer<Any?>
        scopeCopy.accept(testClazz.cast(testObj))
    }
}
