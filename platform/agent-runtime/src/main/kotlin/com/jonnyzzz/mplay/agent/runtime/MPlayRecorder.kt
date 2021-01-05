@file:Suppress("unused", "UNUSED_PARAMETER", "KDocUnresolvedReference")

package com.jonnyzzz.mplay.agent.runtime

/**
 * An instance of that type is injected to the intercepted class,
 * this instance is used to record method activities
 */
interface MPlayRecorder {
    /**
     * Executed on every recorded method call to mark
     * the method execution start
     * @param methodName the method name that is running
     * @param jvmMethodDescriptor the JVM non-generic method descriptor (parameter types + return type)
     */
    fun onMethodEnter(
        methodName: String,
        jvmMethodDescriptor: String,
    ): MPlayMethodCallRecorder
}
