package org.jonnyzzz.mplay.agent

import net.bytebuddy.implementation.bind.annotation.*
import java.lang.Exception
import java.util.concurrent.Callable

class MPlayInterceptor {

    @RuntimeType
    @Throws(Exception::class)
    fun delegate(
        @SuperCall zuper: Callable<*>,
        @This thiz: Any,
        @AllArguments vararg args: Any
    ): Any? {
        println("Calling of $thiz with ${args.contentToString()}")
        //serialize arguments
        val r = zuper.call()
        println("Calling of $thiz with ${args.contentToString()}: result $r")
        return r
    }

    @RuntimeType
    @Throws(Exception::class)
    fun delegate(
        @SuperCall zuper: Callable<*>,
        @This thiz: Any,
        @Argument(0) a: Int
    ): Any? {
        println("Calling of $thiz with x[$a]")
        //serialize arguments
        val r = zuper.call()
        println("Calling of $thiz with x[$a]: result $r")
        return r
    }
}
