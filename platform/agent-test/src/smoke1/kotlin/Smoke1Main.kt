package com.jonnyzzz.mplay.agent.smoke1

import java.io.File

class SmokeTestClass {
    fun callMe(x: Int) : Int {
        println("callMe($x)")
        return x
    }
}

class SmokeTestClass2 {
    fun callMe2(x: String) : Long {
        println("callMe($x)")
        return 42_42
    }
}

fun main() {
    println("It works!")
    println("Classpath:")
    System.getProperty("java.class.path").split(File.pathSeparator).forEach {
        println("  $it")
    }

    //test agent is able to inject the class to the application
    val clazz = Class.forName("com.jonnyzzz.mplay.agent.runtime.MPlayRecorderFactory")
    println("The loaded class $clazz - ${clazz.classLoader}")

    val r = SmokeTestClass().callMe(42)
    SmokeTestClass2().callMe2("42")
    require(r == 42)
}
