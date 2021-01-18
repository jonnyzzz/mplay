@file:Suppress("UNUSED_PARAMETER", "unused")

package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.config.MethodRef
import com.jonnyzzz.mplay.agent.config.ctor
import com.jonnyzzz.mplay.agent.primaryConstructors
import org.junit.Assert
import org.junit.Test
import org.objectweb.asm.Type

class ClassInterceptorTest {
    @Test
    fun `primary constructor detection - default`() {
        class WithOneConstructor
        val constructors = primaryConstructors<WithOneConstructor>().map { it.toMethodRef() }
        Assert.assertEquals(listOf(MethodRef.ctor("()V")), constructors)
    }

    @Test
    fun `primary constructor detection - single`() {
        class WithOneConstructor(x: String)

        val constructors = primaryConstructors<WithOneConstructor>().map { it.toMethodRef() }
        Assert.assertEquals(listOf(MethodRef.ctor( "(Ljava/lang/String;)V")), constructors)
    }

    @Test
    fun `primary constructor detection - single generic`() {
        class WithOneConstructor<R>(x: R)

        val constructors = primaryConstructors<WithOneConstructor<*>>().map { it.toMethodRef() }
        Assert.assertEquals(listOf(MethodRef.ctor("(Ljava/lang/Object;)V")), constructors)
    }

    @Test
    fun `primary constructor detection - single generic with constraint`() {
        class WithOneConstructor<R : Runnable>(x: R)

        val constructors = primaryConstructors<WithOneConstructor<*>>().map { it.toMethodRef() }
        Assert.assertEquals(listOf(MethodRef.ctor("(Ljava/lang/Runnable;)V")), constructors)
    }

    @Test
    fun `primary constructor detection - multiple with base`() {
        class WithOneConstructor() {
            constructor(int: Int): this()
            constructor(long: Long) : this()
        }

        val constructors = primaryConstructors<WithOneConstructor>().map { it.toMethodRef() }
        Assert.assertEquals(listOf(MethodRef.ctor("()V")), constructors)
    }

    @Test
    fun `primary constructor detection - multiple with base calling base`() {
        class WithOneConstructor(y: WithOneConstructor?) {
            constructor(int: Int): this(null)
            constructor(long: Long) : this(WithOneConstructor(WithOneConstructor(42L)))
        }

        val constructors = primaryConstructors<WithOneConstructor>().map { it.toMethodRef() }
        val internalName = "L" + Type.getInternalName(WithOneConstructor::class.java) + ";"
        Assert.assertEquals(listOf(MethodRef.ctor("($internalName)V")), constructors)
    }

    @Test
    fun `primary constructor detection - multiple no base`() {
        class WithOneConstructor {
            constructor(int: Int, r : String) { ArrayList<Int>().add(int) }
            constructor(long: Long) { java.lang.Object() }
        }

        val constructors = primaryConstructors<WithOneConstructor>().map { it.toMethodRef() }
        Assert.assertEquals(listOf(
            MethodRef.ctor("(ILjava/lang/String;)V"),
            MethodRef.ctor("(J)V"),
        ), constructors)
    }

    @Test
    fun `primary constructor detection - with private delegation`() {
        class WithOneConstructor {
            private constructor(int: Int, r : String) { ArrayList<Int>().add(int) }
            constructor(long: Long) : this(long.toInt(), long.toString())
        }

        val constructors = primaryConstructors<WithOneConstructor>().map { it.toMethodRef() }
        Assert.assertEquals(listOf(
            MethodRef.ctor("(ILjava/lang/String;)V"),
        ), constructors)
    }

    @Test
    fun `primary constructor detection - with protected delegation`() {
        class WithOneConstructor {
            private constructor(int: Int, r : String) { ArrayList<Int>().add(int) }
            constructor(long: Long) : this(long.toInt(), long.toString())
        }

        val constructors = primaryConstructors<WithOneConstructor>().map { it.toMethodRef() }
        Assert.assertEquals(listOf(
            MethodRef.ctor("(ILjava/lang/String;)V"),
        ), constructors)
    }
}
