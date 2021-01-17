package com.jonnyzzz.mplay.agent.runtime

/**
 * The constructor parameters visitor and the instance
 * recorder factory.
 *
 * This class created from the
 * [MPlayInstanceRecorderBuilder.newConstructorRecorder] method.
 *
 * Every constructor parameter is visited only once in the
 * declaration order via the calls to the `visit*` methods
 * of [MPlayValuesVisitor].
 *
 * @see MPlayInstanceRecorderBuilder.newConstructorRecorder
 * @see MPlayValuesVisitor
 */
interface MPlayConstructorRecorder : MPlayValuesVisitor {
    /**
     * Visits the instance of the class that is created
     * with methods recording enabled.
     *
     * NOTE, this is called from the constructor, so it
     * is probably not safe to use this instance methods
     * directly in the visitor.
     */
    fun visitInstance(instance: Any) = Unit

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
