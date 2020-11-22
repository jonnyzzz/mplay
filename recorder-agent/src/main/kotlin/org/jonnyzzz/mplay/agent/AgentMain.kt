package org.jonnyzzz.mplay.agent

import net.bytebuddy.agent.builder.AgentBuilder
import net.bytebuddy.description.type.TypeDefinition
import net.bytebuddy.description.type.TypeDescription
import net.bytebuddy.dynamic.DynamicType
import net.bytebuddy.implementation.MethodDelegation.to
import net.bytebuddy.matcher.ElementMatcher
import net.bytebuddy.utility.JavaModule
import java.lang.instrument.Instrumentation

import net.bytebuddy.matcher.ElementMatchers


class MPlayAgent {
    companion object {
        @JvmStatic
        fun premain(
            arguments: String?,
            instrumentation: Instrumentation
        ) {
            val args = (arguments ?: "")
                .split(";")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .map {
                    val kv = it.split("=", limit = 2)
                    kv[0] to kv.getOrNull(1)
                }.toMap().toSortedMap()

            premainImpl(MPlayAgentArgs(args), instrumentation)
        }
    }
}

class MPlayAgentArgs(
    private val kv: Map<String, String?>
) {
    fun typeMatcher() = ElementMatcher<TypeDefinition> { type ->
        !type.isPrimitive && !type.isInterface /*TODO: test type*/
    }
}

private fun premainImpl(
    args: MPlayAgentArgs,
    instrumentation: Instrumentation
) {
    val interceptor = MPlayInterceptor()
    AgentBuilder.Default()
        .type(args.typeMatcher())
        .transform { builder: DynamicType.Builder<*>,
                     _: TypeDescription,
                     _: ClassLoader,
                     _: JavaModule ->
            builder.method(
                ElementMatchers.not(ElementMatchers.isDeclaredBy(Object::class.java))
            ).intercept(to(interceptor))
        }
        .installOn(instrumentation)
}

