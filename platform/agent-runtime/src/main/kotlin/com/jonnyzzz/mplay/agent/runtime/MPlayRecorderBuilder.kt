package com.jonnyzzz.mplay.agent.runtime

interface MPlayRecorderBuilder : MPlayValuesVisitor {
    /**
     * Visits which constructor is executed to create
     * the class, using the JVM method descriptor
     */
    fun visitConstructorDescriptor(descriptor: String)

    /**
     * Visits the instance of the class that is created
     * with methods recording enabled.
     *
     * NOTE, this is called from the constructor, so it
     * is probably not safe to use this instance methods
     * directly in the visitor.
     */
    fun visitInstance(instance: Any)

    /**
     * Marks that all constructor parameters were send.
     * Before calling this method, the implementation calls
     * the respective `visit*` methods from [MPlayValuesVisitor] to pass
     * all parameter values in the order of parameters in the
     * JVM metadata.
     *
     * Once the method is executed successfully or with an exception,
     * the implementation calls [MPlayMethodResultRecorder.commit]
     * to complete the method execution recording
     */
    fun visitConstructorParametersComplete(): MPlayRecorder
}
