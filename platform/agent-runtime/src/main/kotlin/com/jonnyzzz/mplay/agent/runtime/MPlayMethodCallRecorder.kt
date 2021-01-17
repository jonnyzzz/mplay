package com.jonnyzzz.mplay.agent.runtime

/**
 * An entry point interface to record method and it's parameters.
 * The implementation is also responsible to intercept the parameter
 * values, which are send to the original method.
 *
 * Usually created from
 * [MPlayRecorder.onMethodEnter] to process
 * a given method parameters.
 *
 * @see MPlayRecorder.onMethodEnter
 */
interface MPlayMethodCallRecorder : MPlayValuesVisitor {
    /**
     * Marks that all parameters were send. Before calling this
     * method, the implementation calls the the respective
     * `visit*` methods from [MPlayValuesVisitor] to pass
     * all parameter values in the order of parameters in the
     * JVM metadata.
     *
     * Once the method is executed successfully or with an exception,
     * the implementation calls [MPlayMethodResultRecorder.commit]
     * to complete the method execution recording
     */
    fun visitParametersComplete(): MPlayMethodResultRecorder
}
