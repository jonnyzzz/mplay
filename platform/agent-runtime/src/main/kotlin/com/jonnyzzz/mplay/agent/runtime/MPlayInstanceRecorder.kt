@file:Suppress("unused", "UNUSED_PARAMETER", "KDocUnresolvedReference")

package com.jonnyzzz.mplay.agent.runtime

/**
 * An instance of that type is injected to the intercepted class,
 * this instance is used to record method activities
 *
 * Usually created via
 * [MPlayConstructorCallRecorder.newInstanceRecorder].
 * This instance is cached in the recording type and handles
 * all method calls
 *
 * @see MPlayConstructorCallRecorder.newInstanceRecorder
 */
interface MPlayInstanceRecorder {
    /**
     * Executed on every recorded method call on the original class,
     * to get the actual implementation of the method recorder.
     *
     * @param methodName the method name that is running
     * @param descriptor the JVM non-generic method descriptor (parameter types + return type)
     */
    fun newMethodRecorder(
        methodName: String,
        descriptor: String,
    ): MPlayMethodCallRecorder
}
