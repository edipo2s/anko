package org.jetbrains.android.anko.templates

import org.jtwig.JtwigModel
import org.jtwig.JtwigTemplate
import org.jtwig.environment.DefaultEnvironmentConfiguration
import org.jtwig.environment.EnvironmentConfigurationBuilder
import org.jtwig.functions.FunctionRequest
import org.jtwig.functions.SimpleJtwigFunction
import org.jtwig.spaceless.SpacelessExtension
import org.jtwig.spaceless.configuration.DefaultSpacelessConfiguration
import java.io.File
import java.math.BigDecimal

class JtwigTemplateProvider : TemplateProvider {
    override val extension = "twig"

    private val templateCache = hashMapOf<File, JtwigTemplate>()

    override fun render(templateFile: File, args: Map<String, Any?>): String {
        val model = JtwigModel.newModel(args)
        return getTemplate(templateFile).render(model)
    }

    private fun getTemplate(templateFile: File) = templateCache.getOrPut(templateFile) {
        val configuration = EnvironmentConfigurationBuilder.configuration()
        configuration.extensions().add(SpacelessExtension(DefaultSpacelessConfiguration()))
        configuration.functions().add(object : SimpleJtwigFunction() {
            override fun name() = "range"

            override fun execute(request: FunctionRequest): Any {
                request.minimumNumberOfArguments(2).maximumNumberOfArguments(2)

                val min = parseInt(request.arguments[0])
                val max = parseInt(request.arguments[1])

                return min..max
            }

            private fun parseInt(o: Any) = when (o) {
                is BigDecimal -> o.toInt()
                is Int -> o
                else -> throw IllegalArgumentException(
                        "Illegal argument type: ${o.javaClass.canonicalName}, expected Integer or BigDecimal")
            }
        })

        JtwigTemplate.fileTemplate(templateFile, configuration.build())
    }
}