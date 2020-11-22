package org.jonnyzzz.mplay.agent.smoke1

class SmokeTestClass {
    fun callMe(x: Int) : Int {
        println("callMe($x)")
        return x
    }
}

fun main() {
    println("It works!")
    val r = SmokeTestClass().callMe(42)
    require(r == 42)
}
