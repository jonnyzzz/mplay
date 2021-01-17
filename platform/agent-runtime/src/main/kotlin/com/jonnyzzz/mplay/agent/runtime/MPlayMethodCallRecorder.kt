package com.jonnyzzz.mplay.agent.runtime

/**
 * An entry point interface to record method and it's parameters.
 *
 * The implementation can also replace original parameter
 * values, which are send to the original method.
 *
 * Usually created from
 * [MPlayInstanceRecorder.newMethodRecorder] to
 * record a given method call
 *
 * @see MPlayInstanceRecorder.newMethodRecorder
 */
interface MPlayMethodCallRecorder : MPlayValuesVisitor {
    /**
     * Before calling this method, the implementation calls
     * the the respective `visit*` methods from [MPlayValuesVisitor]
     * to pass all parameter values in the order of parameters in the
     * JVM metadata. The implementation may also replace the original
     * parameter values.
     *
     * This method creates the [MPlayMethodResultRecorder] just before
     * the original method is started
     */
    fun newRunningMethodRecorder(): MPlayRunningMethodRecorder
}
