@file:Suppress("unused")

package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.config.MPlayConfiguration
import org.hamcrest.core.IsInstanceOf
import org.junit.Assert
import org.junit.Test

class ConfigClassTest {
    @Test
    fun testCreateConfigurationClass() {
        class ToProxy
        class Configuration: MPlayConfiguration<ToProxy>

        val config = ConfigurationClass.fromConfigClass(Configuration::class.java)

        Assert.assertEquals(ToProxy::class.java, config.interceptedRawType)
        Assert.assertEquals(Configuration::class.java, config.configClass)
        Assert.assertThat(config.configuration, IsInstanceOf(Configuration::class.java))
        Assert.assertEquals(emptySet<Class<*>>(), config.baseClassesToIntercept)
    }

    @Test
    fun testCreateConfigurationClassGeneric() {
        class ToProxy<R> {
            fun getR() : R = error("Will not be executed")
        }

        class Configuration<R>: MPlayConfiguration<ToProxy<R>>

        val config = ConfigurationClass.fromConfigClass(Configuration::class.java)

        Assert.assertEquals(ToProxy::class.java, config.interceptedRawType)
        Assert.assertEquals(Configuration::class.java, config.configClass)
        Assert.assertThat(config.configuration, IsInstanceOf(Configuration::class.java))
        Assert.assertEquals(emptySet<Class<*>>(), config.baseClassesToIntercept)
    }

    @Test
    fun testCreateConfigurationClassWithBase() {
        open class Base {
            fun getQ() = 42
        }

        class ToProxy<R> : Base() {
            fun getR() : R = error("Will not be executed")
        }

        class Configuration<R>: MPlayConfiguration<ToProxy<R>>

        val config = ConfigurationClass.fromConfigClass(Configuration::class.java)

        Assert.assertEquals(ToProxy::class.java, config.interceptedRawType)
        Assert.assertEquals(Configuration::class.java, config.configClass)
        Assert.assertThat(config.configuration, IsInstanceOf(Configuration::class.java))
        Assert.assertEquals(setOf<Class<*>>(Base::class.java), config.baseClassesToIntercept)
    }

    @Test
    fun testCreateConfigurationClassWithBaseAndBound() {
        open class Base {
            fun getQ() = 42
        }

        class ToProxy<R> : Base() {
            fun getR() : R = error("Will not be executed")
        }

        class Configuration<R>: MPlayConfiguration<ToProxy<R>> {
            override val upperLimit: Class<*> = Base::class.java
        }

        val config = ConfigurationClass.fromConfigClass(Configuration::class.java)

        Assert.assertEquals(ToProxy::class.java, config.interceptedRawType)
        Assert.assertEquals(Configuration::class.java, config.configClass)
        Assert.assertThat(config.configuration, IsInstanceOf(Configuration::class.java))
        Assert.assertEquals(setOf<Class<*>>(), config.baseClassesToIntercept)
    }
}

