package com.jonnyzzz.mplay.agent.runtime

/**
 * The constructor parameters visitor and the instance
 * recorder factory.
 *
 * This class created from the
 * [MPlayConstructorRecorder.newConstructorCallRecorder] method.
 *
 * Every constructor parameter is visited only once in the
 * declaration order via the calls to the `visit*` methods
 * of [MPlayValuesVisitor].
 *
 * @see MPlayConstructorRecorder.newConstructorCallRecorder
 * @see MPlayValuesVisitor
 */
interface MPlayConstructorCallRecorder : MPlayValuesVisitor {
    /**
     * Creates the recorder for a given instance. This object
     * will be stored in a field of the class, where recording
     * is injected.
     *
     * Before calling this method, the implementation calls
     * the respective `visit*` methods from [MPlayValuesVisitor] to pass
     * all parameter values in the order of parameters in the
     * JVM metadata.
     */
    fun newInstanceRecorder(): MPlayInstanceRecorder
}
