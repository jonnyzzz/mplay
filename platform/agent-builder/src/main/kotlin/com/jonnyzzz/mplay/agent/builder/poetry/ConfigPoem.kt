package com.jonnyzzz.mplay.agent.builder.poetry

import com.jonnyzzz.mplay.config.MPlayConfiguration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

fun generateConfigApiPoem(interceptType: Type): String {

    return when (interceptType) {
        is Class<*> -> generateConfigApiPoemForClass(interceptType)
        is ParameterizedType -> generateConfigApiPoemForGeneric(interceptType)
        else -> error("Unsupported object type for API generation: $interceptType")
    }


}

private fun generateConfigApiPoemForGeneric(interceptType: ParameterizedType): String {
    return generateConfigApiPoemForClass(interceptType.rawType as Class<*>, interceptType)
}

private fun generateConfigApiPoemForClass(
    interceptRawType: Class<*>,
    interceptType: Type = interceptRawType
): String {

    val shortName = interceptRawType.simpleName.replace("$", "_")

    val file = FileSpec.builder(
        "com.jonnyzzz.mplay.poem",
        "poem-$shortName"
    )

    val type = TypeSpec.interfaceBuilder("MPlayRecorderApiFor$shortName")

    //make it implement our base interface
    type.addSuperinterface(
        MPlayConfiguration::class.asClassName().parameterizedBy(interceptType.asTypeName())
    )

    for (ctor in interceptRawType.constructors) {
        if (!Modifier.isPublic(ctor.modifiers) || Modifier.isStatic(ctor.modifiers)) continue
        type.addFunction(generateConstructorWrapper(ctor))
    }

    file.addType(type.build())
    return buildString { file.build().writeTo(this) }
}

private fun generateConstructorWrapper(ctor: Constructor<*>) = FunSpec.builder("newDriver").also { f ->
    val generic = ctor.genericParameterTypes
    val names = ctor.parameters.map { it.name }

    generic.zip(names).forEach { (type, name) ->
        f.addParameter(
            name = name,
            type = type.asTypeName()
        )
    }

    f.returns(Any::class.asTypeName().copy(nullable = true))
    f.addCode("return null")

}.build()
