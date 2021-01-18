@file:Suppress("unused", "UNUSED_PARAMETER")

package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.config.MethodRef
import com.jonnyzzz.mplay.agent.config.ctor
import com.jonnyzzz.mplay.config.MPlayConfiguration
import org.junit.Assert
import org.junit.Test
import java.lang.reflect.Method

class AgentConfigMethodTest {

    @Test
    fun testInterfaceDefaultMethod() {
        val config = ConfigurationClass.fromClass<ClassWithDefaultMethod>()
        Assert.assertEquals("${config.methodsToIntercept}", 1, config.methodsToIntercept.size)

        val method: Method = config.methodsToIntercept.single()
        val agentConfig = config.toInterceptMethodTask(method)

        val ref = MethodRef("fun", "(Ljava/lang/Object;)I")
        Assert.assertEquals(ref, agentConfig.methodRef)
        Assert.assertEquals(TestInterfaceWithDefaultImpl::class.java.name, agentConfig.defaultMethodOfInterface)

        val baseConfig = config.toImplementMethodTask(method)
        Assert.assertEquals(null, baseConfig)

        val agent = ConfigurationClasspath(listOf(config)).toAgentConfig()
        println(agent)
        Assert.assertEquals(0, agent.classesToOpenMethods.map { it.classNameToIntercept }.size)
        Assert.assertEquals(1, agent.classesToRecordEvents.size)
        Assert.assertEquals(0, agent.classesToOpenMethods.size)
    }

    @Test
    fun testBaseClassMethod() {
        open class Base {
            fun baseMethod() {}
        }
        class Inh : Base()

        val config = ConfigurationClass.fromClass<Inh>()
        Assert.assertEquals("${config.methodsToIntercept}", 1, config.methodsToIntercept.size)

        val method: Method = config.methodsToIntercept.single()
        val agentConfig = config.toInterceptMethodTask(method)

        val ref = MethodRef("baseMethod", "()V")
        Assert.assertEquals(ref, agentConfig.methodRef)
        Assert.assertEquals(null, agentConfig.defaultMethodOfInterface)

        val baseConfig = config.toImplementMethodTask(method)
        Assert.assertEquals(ref, baseConfig?.second?.methodRef)

        val agent = ConfigurationClasspath(listOf(config)).toAgentConfig()
        Assert.assertEquals(listOf(Base::class.java.name), agent.classesToOpenMethods.map { it.classNameToIntercept })
        Assert.assertEquals(1, agent.classesToRecordEvents.size)
        Assert.assertEquals(1, agent.classesToOpenMethods.size)
        Assert.assertEquals(listOf(MethodRef.ctor("()V")), agent.classesToRecordEvents.single().constructorsToIntercept.map { it.methodRef })
    }

    @Test
    fun testBaseClassOpenMethod() {
        open class Base {
            open fun baseMethod() {}
        }
        class Inh : Base()

        val config = ConfigurationClass.fromClass<Inh>()
        Assert.assertEquals("${config.methodsToIntercept}", 1, config.methodsToIntercept.size)
        Assert.assertEquals(listOf(MethodRef.ctor("()V")), config.constructorsToIntercept.map { it.toMethodRef() })

        val method: Method = config.methodsToIntercept.single()
        val agentConfig = config.toInterceptMethodTask(method)

        val ref = MethodRef("baseMethod", "()V")
        Assert.assertEquals(ref, agentConfig.methodRef)

        val baseConfig = config.toImplementMethodTask(method)
        Assert.assertNull(baseConfig)

        val agent = ConfigurationClasspath(listOf(config)).toAgentConfig()
        Assert.assertEquals(1, agent.classesToRecordEvents.size)
        Assert.assertEquals(0, agent.classesToOpenMethods.size)
    }

    @Test
    fun testGenericSignature() {
        class ToProxy<R, Q> {
            fun m(r: R): Q = error("should not be called")
        }

        class Configuration<R, Q> : MPlayConfiguration<ToProxy<R, Q>>

        val config = ConfigurationClass.fromConfigClass<Configuration<*, *>>()
        Assert.assertEquals("${config.methodsToIntercept}", 1, config.methodsToIntercept.size)

        val method: Method = config.methodsToIntercept.single()
        val agentConfig = config.toInterceptMethodTask(method)

        Assert.assertEquals("m", agentConfig.methodRef.methodName)
        Assert.assertEquals("(Ljava/lang/Object;)Ljava/lang/Object;", agentConfig.methodRef.descriptor)
    }

    @Test
    fun testGenericMethodSignature() {
        class ToProxy<R, Q> {
            fun <E> m(e:E, r: R): Q = error("should not be called")
        }

        class Configuration<R, Q> : MPlayConfiguration<ToProxy<R, Q>>

        val config = ConfigurationClass.fromConfigClass<Configuration<*, *>>()
        Assert.assertEquals("${config.methodsToIntercept}", 1, config.methodsToIntercept.size)

        val method: Method = config.methodsToIntercept.single()
        val agentConfig = config.toInterceptMethodTask(method)

        Assert.assertEquals("m", agentConfig.methodRef.methodName)
        Assert.assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", agentConfig.methodRef.descriptor)
    }

    @Test
    fun testOrdinarySignature() {
        class ToProxy {
            fun m(r: String): Long = error("should not be called")
        }

        class Configuration<R, Q> : MPlayConfiguration<ToProxy>

        val config = ConfigurationClass.fromConfigClass<Configuration<*, *>>()
        Assert.assertEquals("${config.methodsToIntercept}", 1, config.methodsToIntercept.size)

        val method: Method = config.methodsToIntercept.single()
        val agentConfig = config.toInterceptMethodTask(method)

        Assert.assertEquals("m", agentConfig.methodRef.methodName)
        Assert.assertEquals("(Ljava/lang/String;)J", agentConfig.methodRef.descriptor)
    }
}
