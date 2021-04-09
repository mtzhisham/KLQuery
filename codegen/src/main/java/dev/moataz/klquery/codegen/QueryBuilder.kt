package dev.moataz.klquery.codegen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.moataz.klquery.util.*
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.type.TypeKind

class QueryBuilder(
    private val info: QueryInfo
) {
    private val queryClassName = info.queryClassName
    private val tripleClass = ClassName("kotlin", "Triple")
    private val tripleClassParametrized = tripleClass.parameterizedBy(
        String::class.asTypeName(),
        Matchers::class.asTypeName(),
        Any::class.asTypeName()
    )

    private fun TypeSpec.Builder.addConstructor() = apply {
        val constructorBuilder =  FunSpec.constructorBuilder()
        constructorBuilder
                .addParameter(
                        ParameterSpec.builder("query", StringBuilder::class.asTypeName().copy(true))
                                .defaultValue("null")
                                .build()
                ).addCode(
                        """
                |closestQueryName= this::class.simpleName?.replace("Query", "")?:"${info.queryClassName}"
                |closestQueryName= closestQueryName.setFirsCharSmall()
                |if(query!= null){
                |this.query = query
                |isCascaded= true                
                |}
                |else {  
                | this.query = StringBuilder("{\"query\":\"query")
                |this.query.append(" {\\r\\n ")
                |this.query.append(closestQueryName)
                |this.query.append(" {\\r\\n ")
                |}             
                """.trimMargin())
                .build()
        addFunction(constructorBuilder.build())
    }
    private fun TypeSpec.Builder.addConstructorClassFields() = apply {
        addProperty(PropertySpec
                .builder(
                        "query", StringBuilder::class.asTypeName(),
                        KModifier.PRIVATE, KModifier.LATEINIT)
                .mutable(true)
                .build())

        addProperty(PropertySpec
                .builder(
                        "isCascaded", Boolean::class.asTypeName(),
                        KModifier.PRIVATE)
                .mutable(true)
                .initializer("false")
                .build())

        addProperty(PropertySpec
                .builder(
                        "closestQueryName", String::class.asTypeName(),
                        KModifier.PRIVATE)
                .mutable(true)
                .initializer("\"\"")
                .build())
    }
    private fun TypeSpec.Builder.addFields() = apply {
        info.queryClassElement.enclosedElements.forEach {
            if (it.kind == ElementKind.FIELD){
                val type = it.asType().toString()
                val collectionOf = if (type.contains("<")) type.substringAfter("<").substringBefore('>') else type
                val collectionOfPackageName= collectionOf.substringBeforeLast(".")
                val collectionOfTypeName= collectionOf.substringAfterLast(".")

                if(ClassName(collectionOfPackageName, collectionOfTypeName).isPrimitive()){
                    addProperty(PropertySpec
                            .builder(
                                    it.simpleName.toString(),
                                    Unit::class,
                                    KModifier.PUBLIC)
                            .getter(FunSpec.getterBuilder().addStatement("_${it.simpleName}()").build())
                            .build())
                    addFunction(FunSpec.builder("_${it.simpleName}")
                            .addStatement("query.append(\"${it.simpleName}\")")
                            .addStatement("query.append( \"\\\\r\\\\n \")")
                            .addModifiers(KModifier.PRIVATE)
                            .build())
                }  else {
                    addProperty(PropertySpec.builder(
                            "_" + it.simpleName.toString(),
                            ClassName(collectionOfPackageName, collectionOfTypeName + Constants.CLASS_POSTFIX_NAME),
                            KModifier.PRIVATE, KModifier.LATEINIT)
                            .mutable(true)
                            .build())
                    addFunction(
                            FunSpec
                                    .builder("${it.simpleName}" + Constants.CLASS_POSTFIX_NAME)
                                    .receiver(ClassName(info.queryPackage, info.queryClassName))
                                    .addModifiers(KModifier.PUBLIC)
                                    .addParameter(ParameterSpec(
                                            "block", LambdaTypeName.get(
                                            ClassName(
                                                    collectionOfPackageName,
                                                    collectionOfTypeName
                                                            + Constants.CLASS_POSTFIX_NAME
                                            ),
                                            parameters = listOf(),
                                            returnType = Unit::class.asClassName()
                                    )
                                    )
                                    )
                                    .addCode(
                                            """
                                    |this._${it.simpleName}= ${collectionOfTypeName}Query(query = query)
                                    |query.append("${it.simpleName}")
                                    |query.append(" {\\r\\n ")
                                    |this._${it.simpleName}.apply(block)
                                    |query.append(" }\\r\\n ")
                                    |return this
                                    """.trimMargin())
                                    .returns(ClassName(info.queryPackage, info.queryClassName)).build()
                    )
                }
            }
        }
    }
    private fun TypeSpec.Builder.addArguments() = apply {
        addFunction(FunSpec.builder("setArguments")
            .addParameter(
                ParameterSpec.builder("args", tripleClassParametrized, KModifier.VARARG)
                    .build()
            )
            .addCode("""
                |val argsBuilder: StringBuilder = StringBuilder()
                |argsBuilder.append("(")
                |argsBuilder.append("filter:{ ")
                |args.forEachIndexed { index, pair ->val (name, matcher, value) = pair
                |argsBuilder.append(name)
                |argsBuilder.append(" : ")
                |when(matcher){
                |Matchers.TO-> {argsBuilder.append(""+ if(value::class.simpleName == "String") "\\\\"${'$'}value\\\\"" else ""+ value)
                |argsBuilder.append(if (args.size -1 != index) "," else "" )}
                |Matchers.MATCH-> {argsBuilder.append( "{match: " + if(value::class.simpleName == "String") "\\\\"${'$'}value\\\\"" else ""+ value + "}")
                |argsBuilder.append(if (args.size -1 != index) "," else "" )}
                |Matchers.EQ-> {argsBuilder.append( "{eq: " + if(value::class.simpleName == "String") "\\\\"${'$'}value\\\\"" else ""+ value + "}")
                |argsBuilder.append(if (args.size -1 != index) "," else "" )}
                |Matchers.FROMTO-> {
                |val fromTypeString= (value as dev.moataz.klquery.util.KQLRange).from.second::class.simpleName == "String"
                |val toTypeString= (value as dev.moataz.klquery.util.KQLRange).to.second::class.simpleName == "String"
                |argsBuilder.append( "{from: " + if (fromTypeString) "\\\\"${'$'}value.from.second\\\\"" else ""+ value.from.second )
                |argsBuilder.append(", ")
                |argsBuilder.append("{from: " + if (toTypeString) "\\\\"${'$'}value.to.second\\\\"" else ""+ value.to.second )
                |argsBuilder.append("}")
                |argsBuilder.append(if (args.size -1 != index) "," else "" )
                |}
                |Matchers.IN-> {argsBuilder.append( "{in: " + if(value::class.simpleName == "String") "\"${'$'}value\"" else ""+ value + "}")
                |argsBuilder.append(if (args.size -1 != index) "," else "" )}
                |Matchers.CUSTOM-> {argsBuilder.append( "{"+ value + "}")
                |argsBuilder.append(if (args.size -1 != index) "," else "" )}
                |}
                |}
                |argsBuilder.append(" })")
                |query.deleteRange(query.lastIndexOf("{\\r\\n "), query.length-1)
                |query.append(argsBuilder)
                |query.append(" {\\r\\n ")
            """.trimMargin())
            .build()
        )
    }
    private fun TypeSpec.Builder.addQueryBuilder() = apply {
        addFunction(
            FunSpec.builder("buildQueryString")
                .addCode(
                    """                 
                    |if(!isCascaded) query.append(" }\\r\\n ")
                    |query.append( "}\\r\\n ")
                    |query.append(" \"}")
                    |return query.toString()
                    """.trimMargin())
                .returns(String::class).build()
        )
    }

    fun buildClass() = TypeSpec.classBuilder(queryClassName)
            .addConstructor()
            .addConstructorClassFields()
            .addFields()
            .addArguments()
            .addQueryBuilder()
            .build()

    fun buildDSL(): FunSpec {
        return FunSpec.builder(info.queryClassName.setFirsCharSmall())
        .addParameter(
            ParameterSpec(
                "block", LambdaTypeName.get(
                    ClassName(
                        info.queryPackage,
                        info.queryClassName
                    ), parameters = listOf(), returnType = Unit::class.asClassName()
                )
            )
        )
        .addStatement("return ${info.queryClassName}().apply(block)")
        .returns(ClassName(info.queryPackage, info.queryClassName)).build()}
}