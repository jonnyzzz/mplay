package com.jonnyzzz.mplay.config

/**
 * A marker annotation of the class which implements
 * the [MPlayConfiguration]. The class can be a generic
 * one if the original type has generic parameters.
 */
annotation class MPlayConfig

/**
 * The entry point for the MPlay configuration,
 * this interface can be implemented as a class with
 * default constructor or as a Kotlin Object.
 *
 * The [T] is the type of the class, which will be
 * enhanced to record all calls from it. The implementation
 * class can have generic parameters the same was as
 * [T] does. The generic parameters be replaced with
 * the upper-bound types as well.
 *
 * The implementations should be marked with [MPlayConfig]
 * annotation to let the MPlay use the implementation,
 * it assumes a class with default constructor or a
 * Kotlin object. Please note, the [T] type must not
 * be a generic parameter in the implementation class
 *
 * The implementation may define a method called `driver`
 * with the type [T] constructor parameters to provide
 * the actual helpers for the parameters and return
 * types serialization.
 *  - parameters should go in the same order as in original constructor of [T]
 *  - parameters can be generic
 *  - the erased signature of the method should match the constructor
 *  - the return type of the method should be `MPlayConfigurationDriver<T>?`
 *  - returned `null` would make to skip the instance recording
 *
 * Example:
 * ```
 *  fun newDriver(<constructorParams>): MPlayConfigurationDriver<T>?
 * ```
 *
 *
 * @see MPlayConfig
 */
interface MPlayConfiguration<T> {
    /**
     * Specified **inclusive** upper bound in base classes
     * hierarchy to limit the number of methods that will
     * be intercepted. The [Any] or [java.lang.Object] is
     * the default value
     */
    val upperLimit: Class<*> get() = java.lang.Object::class.java

    /**
     * If `true` the MPLay recorder will also record all
     * nested calls (maybe of a different type) that happens during
     * the execution of a method from [T]
     */
    //TODO: not implemented yet, good feature
    val allowRecordingNestedCalls : Boolean get() = false
}

/**
 * A helper class, that is used to provide a specific
 * handling of the methods of a given type [T]. Returned
 * from methods of [MPlayConfiguration<T>] named `
 */
interface MPlayConfigurationDriver<T> {
    /**
     * This method is executed to let the driver change
     * constructor parameters for serialization.
     * It is up to the same driver to deserialize the changed parameters
     * when re-playing the code
     */
    fun mapConstructorParamsForSerialization(args: List<Any?>) : List<Any?> = args
}
