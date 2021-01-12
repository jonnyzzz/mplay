package com.jonnyzzz.mplay.recorder

import com.jonnyzzz.mplay.config.MPlayConfiguration
import java.net.URL
import java.net.URLClassLoader
import java.util.concurrent.ConcurrentHashMap

class RecorderConfigLoader(
    private val classpath: Array<out URL>
) {
    private val ourLoaders = ConcurrentHashMap<ClassLoader, ClassLoader>()

    fun loadConfigFor(instance: Any, configClassName: String) : Class<out MPlayConfiguration<*>> {
        val classLoader = instance.javaClass.classLoader
        val loader = ourLoaders.computeIfAbsent(classLoader) {
            URLClassLoader(classpath, it)
        }

        val configClazz = loader.loadClass(configClassName)

        val configInterfaceClass = MPlayConfiguration::class.java
        require(configInterfaceClass.isAssignableFrom(configClazz)) {
            "The config class $configClassName from $classLoader should " +
                    "implement ${configInterfaceClass.name} from ${configInterfaceClass.classLoader}"
        }

        @Suppress("UNCHECKED_CAST")
        return configClazz as Class<out MPlayConfiguration<*>>
    }
}
