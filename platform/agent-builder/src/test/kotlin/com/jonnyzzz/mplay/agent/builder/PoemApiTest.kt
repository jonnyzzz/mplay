@file:Suppress("unused", "UNUSED_PARAMETER")

package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.builder.poetry.generateConfigApiPoem
import org.junit.Test

class PoemApiTest {

    @Test
    fun testSimplePoem() {
        class Test(val x: String) {
            fun test(): Int = x.hashCode()
        }

        val poem = generateConfigApiPoem(Test::class.java)
        println(poem)
    }

    @Test
    fun testGenericPoem() {
        open class B1
        open class B2

        class Test<B : B1>(val x: String, b: B) {
            fun <R : B2> test(b: B): R = TODO()
        }

        val poem = generateConfigApiPoem(Test::class.java)
        println(poem)
    }

}