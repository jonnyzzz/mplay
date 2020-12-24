package org.jonnyzzz.mplay.agent.smoke1

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
    val r = SmokeTestClass().callMe(42)
    SmokeTestClass2().callMe2("42")
    require(r == 42)
}
