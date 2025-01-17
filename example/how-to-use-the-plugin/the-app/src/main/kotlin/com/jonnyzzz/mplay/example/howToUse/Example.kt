package com.jonnyzzz.mplay.example.howToUse

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        println("This is the example app for the MPlay")

        val e = ExampleService()
        repeat(100) {
            e.exampleMethod("param-$it", it * it + 1)
        }
    }
}

open class SomeWeirdBase {
    fun skipMe() { }
}

open class ExampleServiceBase : SomeWeirdBase() {
    fun yetAnotherMethodToIntercept() { }
}

class ExampleService  : ExampleServiceBase() {
    fun exampleMethod(s: String, i: Int) {
        // this is the example method that we will patch on the load
        // to attach recording of the events
        Thread.sleep((100L..300L).random())
    }
}


class GenericService<R, Y> {
    fun putR(r: R) = Unit
    fun getY(): Y? = null
    fun test(r: R, y: Y) = Unit
}

