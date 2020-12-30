package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.config.MPlayConfiguration
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Proxy
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

        Proxy.newProxyInstance(
            ConfigurationClass::class.java.classLoader,
            arrayOf(MPlayConfiguration::class.java),
        ) { _, method, args ->
            instance::class.java
                .getMethod(method.name, *method.parameterTypes)
                .invoke(instance, *args ?: arrayOf())
        } as MPlayConfiguration<*>
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

    val publicMethods by lazy {
        interceptedRawType.methods.filter { it.declaringClass in allIncludedInterceptedTypeBases }
    }

    val methodParameterTypes: List<Type> by lazy {
        publicMethods
            .flatMapTo(HashSet()) { it.genericParameterTypes.toList() }
            .sortedBy { it.typeName }
    }
}

fun ConfigurationClass.Companion.fromConfigClass(
    classpath: ConfigurationClasspath,
    configClazz: Class<*>
): ConfigurationClass {
    if (!classpath.configType.isAssignableFrom(configClazz)) {
        error("The MPlay configuration ${configClazz.name} must directly implement ${MPlayConfiguration::class.java.name}")
    }

    val interceptType = configClazz.genericInterfaces.mapNotNull {
        if (it !is ParameterizedType) return@mapNotNull null
        val rawType = it.rawType as? Class<*> ?: return@mapNotNull null
        if (rawType != classpath.configType) return@mapNotNull null
        it.actualTypeArguments.single()
    }.singleOrNull() ?: error(
        "The MPlay configuration ${configClazz.name} must " +
                "directly implement ${MPlayConfiguration::class.java.name}"
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

