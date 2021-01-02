@file:Suppress("unused", "UNUSED_PARAMETER")

package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.config.MPlayConfiguration
import org.junit.Assert
import org.junit.Test
import java.lang.reflect.Method

class AgentConfigMethodTest {
    @Test
    fun testGenericSignature() {
        class ToProxy<R, Q> {
            fun m(r: R): Q = error("should not be called")
        }

        class Configuration<R, Q> : MPlayConfiguration<ToProxy<R, Q>>

        val config = ConfigurationClass.fromConfigClass<Configuration<*, *>>()
        Assert.assertEquals("${config.methodsToIntercept}", 1, config.methodsToIntercept.size)

        val method: Method = config.methodsToIntercept.single()
        val agentConfig = config.toAgentConfig(method)

        Assert.assertEquals("m", agentConfig.methodName)
        Assert.assertEquals("(Ljava/lang/Object;)Ljava/lang/Object;", agentConfig.jvmMethodDescriptor)
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
        val agentConfig = config.toAgentConfig(method)

        Assert.assertEquals("m", agentConfig.methodName)
        Assert.assertEquals("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", agentConfig.jvmMethodDescriptor)
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
        val agentConfig = config.toAgentConfig(method)

        Assert.assertEquals("m", agentConfig.methodName)
        Assert.assertEquals("(Ljava/lang/String;)J", agentConfig.jvmMethodDescriptor)
    }

}
