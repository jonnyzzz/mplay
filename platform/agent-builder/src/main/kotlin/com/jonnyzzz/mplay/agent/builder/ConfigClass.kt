package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.agent.primaryConstructors
import com.jonnyzzz.mplay.config.MPlayConfiguration
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

data class ConfigurationClass(
    val configClass: Class<*>,

    /**
     * The class that we use to record (and later play) the
     * method calls (aka events)
     */
    val interceptType: Type,

    val interceptedRawType : Class<*>,
) {
    companion object

    val configuration: MPlayConfiguration<*> by lazy {
        val instance = run {
            //case 1 - test if this is a Kotlin object
            runCatching {
                configClass.kotlin.objectInstance?.let { return@run it }
            }

            //case 2 - try to use a constructor
            runCatching {
                configClass.getConstructor().newInstance().let { return@run it }
            }

            error("Failed to create configuration class ${configClass.name}. The class should have default constructor without parameter or be a Kotlin object")
        }

        instance as MPlayConfiguration<*>
    }

    /**
     * This is the set of all base classes of the intercepted class, including
     * the intercepted type itself. It filters the base classes with respect
     * to the [MPlayConfiguration.upperLimit].
     */
    val allIncludedInterceptedTypeBases : Set<Class<*>> by lazy {
        val bases = mutableSetOf<Class<*>>()

        val upperLimit = configuration.upperLimit

        var r = interceptedRawType
        while(r != upperLimit) {
            bases += r
            r = r.superclass ?: break
        }

        bases
    }

    val methodsToIntercept by lazy {
        interceptedRawType.methods.filter { it.declaringClass.isInterface ||  it.declaringClass in allIncludedInterceptedTypeBases }
    }

    /**
     * Returns list of base classes that would be intercepted
     * to allow overriding the methods, it is necessary when the main
     * class will be intercepted to introduce methods logging
     */
    val baseClassesToIntercept : Set<Class<*>> by lazy {
        methodsToIntercept.mapTo(HashSet()) { it.declaringClass } - interceptedRawType
    }

    val methodParameterTypes: List<Type> by lazy {
        methodsToIntercept
            .flatMapTo(HashSet()) { it.genericParameterTypes.toList() }
            .sortedBy { it.typeName }
    }

    val constructorsToIntercept by lazy {
        interceptedRawType.primaryConstructors()
    }
}

fun ConfigurationClass.Companion.fromConfigClass(
    configClazz: Class<*>
): ConfigurationClass {
    val configType = MPlayConfiguration::class.java

    if (!configType.isAssignableFrom(configClazz)) {
        error("The MPlay configuration ${configClazz.name} must directly implement ${configType.name}")
    }

    val interceptType = configClazz.genericInterfaces.mapNotNull {
        if (it !is ParameterizedType) return@mapNotNull null
        val rawType = it.rawType as? Class<*> ?: return@mapNotNull null
        if (rawType != configType) return@mapNotNull null
        it.actualTypeArguments.single()
    }.singleOrNull() ?: error(
        "The MPlay configuration ${configClazz.name} must directly implement ${configType.name}"
    )

    val rawType = when (interceptType) {
        is Class<*> -> interceptType

        is ParameterizedType -> interceptType.rawType as? Class<*>
            ?: error("The actual parametrized class has unexpected raw type: ${interceptType.rawType} in $interceptType")

        else -> error("The actual class must be a class or a generic class, but was $interceptType")
    }

    @Suppress("UNCHECKED_CAST")
    return ConfigurationClass(
        configClass = configClazz,
        interceptType = interceptType,
        interceptedRawType = rawType,
    )
}

