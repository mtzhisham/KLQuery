package dev.moataz.klquery.codegen

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.Import
import dev.moataz.klquery.annotation.KLQuery
import dev.moataz.klquery.util.Constants.CLASS_POSTFIX_NAME
import dev.moataz.klquery.util.hasDeclaredTypes
import java.io.File
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class QueryProcessor: AbstractProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()
    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(KLQuery::class.java.name)
    private val declaredElements: Queue<Element> = LinkedList<Element>()

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: return false
        roundEnv.getElementsAnnotatedWith(KLQuery::class.java)
            .mapNotNull { element ->
                if (element.kind != ElementKind.CLASS) {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only classes can be annotated with @KLQuery \n")
                    return@mapNotNull null
                }
               if (element.hasDeclaredTypes()) {
                   processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "hasDeclaredTypes:  ${element.simpleName} \n")
                   declaredElements.offer(element)
                   return@mapNotNull null
               } else {
                   processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "generateQueryInfo:  ${element.simpleName} \n")
                   generateQueryInfo(element)
                }
            }.map { queryInfo -> buildFileSpec(queryInfo) }
            .forEach { fileSpec -> fileSpec.writeTo(File(kaptKotlinGeneratedDir)) }

            while(declaredElements.peek() != null){
               declaredElements.poll()?.let { element ->
                   if (element.kind != ElementKind.CLASS) {
                       processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only classes can be annotated with @KLQuery \n")
                       return@let
                   }
                   processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "generateQueryInfoFromQueue:  ${element.simpleName} \n")
                      generateQueryInfo(element).let { queryInfo ->
                          buildFileSpec(queryInfo).apply { writeTo(File(kaptKotlinGeneratedDir)) }
                      }
               }
            }
        return true
    }

    private fun buildFileSpec(queryInfo: QueryInfo): FileSpec{
        val fileName = queryInfo.queryClassName
        val fileBuilder = FileSpec.builder(queryInfo.queryPackage, fileName)
        return fileBuilder.addType(QueryBuilder(queryInfo).buildClass())
            .addFunction(QueryBuilder(queryInfo).buildDSL())
                .addImport("dev.moataz.klquery.util", "setFirsCharSmall")
            .build()
    }

    private fun generateQueryInfo(queryClassElement: Element): QueryInfo {
        val itemPackage = processingEnv.elementUtils.getPackageOf(queryClassElement).toString()
        val itemClassName = queryClassElement.asType().toString().drop(itemPackage.length + 1)+CLASS_POSTFIX_NAME
        return QueryInfo(
            queryClassElement,
            itemPackage,
            itemClassName
        )
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}