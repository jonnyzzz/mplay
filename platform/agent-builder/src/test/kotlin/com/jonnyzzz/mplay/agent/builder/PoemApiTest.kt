@file:Suppress("unused", "UNUSED_PARAMETER")

package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.builder.poetry.generateConfigApiPoem
import org.junit.Test

class PoemApiTest {
    class TestSimplePoem(val x: String) {
        fun test(): Int = x.hashCode()
    }

    @Test
    fun testSimplePoem() {
        val poem = generateConfigApiPoem(TestSimplePoem::class.java)
        println(poem)
    }

    open class B1
    open class B2

    class TestGenericPoem<B : B1>(val x: String, b: B) {
        fun <R : B2> test(b: B): R = TODO()
    }

    @Test
    fun testGenericPoem() {
        val poem = generateConfigApiPoem(TestGenericPoem::class.java)
        println(poem)
    }
}