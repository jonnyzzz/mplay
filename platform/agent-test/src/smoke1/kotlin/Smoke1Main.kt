package com.jonnyzzz.mplay.agent.smoke1

import java.io.File
import java.util.function.Consumer

class SmokeTestClass {
    fun callMe(x: Int) : Int {
        println("callMe($x)")
        return x
    }
}

class SuperDuper

class SmokeTestClass2<W>(val c: Consumer<W>) {
    fun callMe2(x: String, w: W): Long {
        println("callMe2($x)$w")
        c.accept(w)
        return 42_42
    }
}

fun main() {
    println("The Smoke1 App is now running")
    println("The Smoke1 App classpath:")

    System.getProperty("java.class.path").split(File.pathSeparator).forEach {
        println("  $it")
    }
    println()

    //test agent is able to inject the class to the application
    val clazz = Class.forName("com.jonnyzzz.mplay.agent.runtime.MPlayRecorderFactory")

    println("Testing the injected class access: loaded class $clazz - loader=${clazz.classLoader}")
    println()

    val r = SmokeTestClass().callMe(42)
    require(r == 42)

    val rr = SmokeTestClass2<SuperDuper> { }.callMe2("42", SuperDuper())
    require(rr == 42_42L)
}
