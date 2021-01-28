package com.jonnyzzz.mplay.agent.builder.poetry

import com.jonnyzzz.mplay.config.MPlayConfiguration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.lang.reflect.*

fun generateConfigApiPoem(interceptType: Class<*>): String {
    val interfaceTypeSpec = generateConfigApiPoemForGeneric(interceptType)

    val file = FileSpec.builder(
        "com.jonnyzzz.mplay.poem",
        "poem-${interfaceTypeSpec.name}"
    )

    file.addType(interfaceTypeSpec)
    return buildString { file.build().writeTo(this) }
}

private fun generateConfigApiPoemForGeneric(rawType: Class<*>): TypeSpec {
    val shortName = rawType.simpleName.replace("$", "_")
    val type = TypeSpec.interfaceBuilder("MPlayRecorderApiFor$shortName")

    val typeVariables = rawType.typeParameters.map { typeArg ->
        require(typeArg is TypeVariable<*>) { "Type parameter of $rawType is not a variable: $typeArg"}
        typeArg.asTypeName() as TypeVariableName
    }

    typeVariables.forEach {
        type.addTypeVariable(it)
    }

    //make it implement our base interface
    type.addSuperinterface(
        MPlayConfiguration::class.asClassName().parameterizedBy(
            rawType.asClassName().let {
                if (typeVariables.isNotEmpty()) {
                    it.parameterizedBy(typeVariables)
                } else it
            }
        )
    )

    for (ctor in rawType.constructors) {
        if (!Modifier.isPublic(ctor.modifiers) || Modifier.isStatic(ctor.modifiers)) continue
        type.addFunction(generateConstructorWrapper(ctor))
    }

    return type.build()
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
