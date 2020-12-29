@file:Suppress("UNUSED_PARAMETER", "unused")

package com.jonnyzzz.mplay

import com.jonnyzzz.mplay.agent.MPlayClass
import com.jonnyzzz.mplay.agent.PrimaryConstructorResolver
import com.jonnyzzz.mplay.agent.fromJdk
import org.junit.Test

class ClassInterceptorTest {
    @Test
    fun `primary constructor detection - default`() {
        class WithOneConstructor

        val clazz = MPlayClass.fromJdk<WithOneConstructor>()

        clazz.newReader()
        val resolver = PrimaryConstructorResolver()
        resolver.visit(clazz)
    }

    @Test
    fun `primary constructor detection - single`() {
        class WithOneConstructor(x: String)

        val clazz = MPlayClass.fromJdk<WithOneConstructor>()

        clazz.newReader()
        val resolver = PrimaryConstructorResolver()
        resolver.visit(clazz)
    }

    @Test
    fun `primary constructor detection - single generic`() {
        class WithOneConstructor<R>(x: R)

        val clazz = MPlayClass.fromJdk<WithOneConstructor<*>>()

        clazz.newReader()
        val resolver = PrimaryConstructorResolver()
        resolver.visit(clazz)
    }

    @Test
    fun `primary constructor detection - single generic with constraint`() {
        class WithOneConstructor<R : Runnable>(x: R)

        val clazz = MPlayClass.fromJdk<WithOneConstructor<*>>()

        clazz.newReader()
        val resolver = PrimaryConstructorResolver()
        resolver.visit(clazz)
    }

    @Test
    fun `primary constructor detection - multiple with base`() {
        class WithOneConstructor() {
            constructor(int: Int): this()
            constructor(long: Long) : this()
        }

        val clazz = MPlayClass.fromJdk<WithOneConstructor>()

        clazz.newReader()
        val resolver = PrimaryConstructorResolver()
        resolver.visit(clazz)

    }

    @Test
    fun `primary constructor detection - multiple no base`() {
        class WithOneConstructor {
            constructor(int: Int, r : String) { ArrayList<Int>().add(int) }
            constructor(long: Long) { java.lang.Object() }
        }

        val clazz = MPlayClass.fromJdk<WithOneConstructor>()

        clazz.newReader()
        val resolver = PrimaryConstructorResolver()
        resolver.visit(clazz)
    }
}
