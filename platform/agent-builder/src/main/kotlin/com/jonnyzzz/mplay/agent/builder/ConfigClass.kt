package com.jonnyzzz.mplay.agent.builder

import com.jonnyzzz.mplay.config.MPlayConfiguration
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

data class ConfigurationClass<T>(
    val configClass: Class<out MPlayConfiguration<T>>,

    /**
     * The class that we use to record (and later play) the
     * method calls (aka events)
     */
    val interceptType: Type,

    val interceptedRawType : Class<*>,
) {
    companion object

    val publicMethods by lazy {
        interceptedRawType.methods.toList()
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
): ConfigurationClass<*> {
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
        configClass = configClazz as Class<out MPlayConfiguration<Any>>,
        interceptType = interceptType,
        interceptedRawType = rawType,
    )
}

