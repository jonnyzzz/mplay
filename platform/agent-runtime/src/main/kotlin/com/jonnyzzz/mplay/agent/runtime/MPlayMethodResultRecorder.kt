package com.jonnyzzz.mplay.agent.runtime

/**
 * Usually created by the [MPlayMethodCallRecorder.visitParametersComplete] call.
 * This type is used to process the method result (or exception) after the execution
 * is completed
 *
 * @see MPlayMethodCallRecorder.visitParametersComplete
 */
interface MPlayMethodResultRecorder : MPlayExceptionVisitor, MPlayValuesVisitor {
    /**
     * Finalizes the method execution.
     *
     * Before this method is executed, the implementation is expected
     * to call:
     *  * nothing for `void` returning methods
     *  * the respective `visit*` method from [MPlayValuesVisitor] to non-void methods
     *  * a method from [MPlayExceptionVisitor] if exception will be thrown
     *
     * All `visit*` methods may replace the values with the returned ones
     */
    fun commit()
}
