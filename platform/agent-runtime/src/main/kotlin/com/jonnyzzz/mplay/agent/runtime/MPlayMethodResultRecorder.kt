package com.jonnyzzz.mplay.agent.runtime

/**
 * The recorder is created before the recording method is
 * started, and finalized once the recording method completes
 *
 * Usually created by the [MPlayRunningMethodRecorder.newMethodResultRecorder] call.
 * This type is used to process the method result (or exception) after the execution
 * is completed
 *
 * @see MPlayRunningMethodRecorder.newMethodResultRecorder
 * @eee MPlayExceptionVisitor
 * @eee MPlayValuesVisitor
 */
interface MPlayMethodResultRecorder : MPlayExceptionVisitor, MPlayValuesVisitor {
    /**
     * Finalizes the method execution.
     *
     * Before this method is executed, the implementation is expected
     * to call:
     *  - nothing for `void` returning methods
     *  - the respective `visit*` method from [MPlayValuesVisitor] to non-void methods
     *  - a method from [MPlayExceptionVisitor] if exception will be thrown
     *
     * All `visit*` methods may replace the values with the returned ones
     */
    fun commit()
}
