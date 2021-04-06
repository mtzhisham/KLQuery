package dev.moataz.klquery.util

import javax.lang.model.element.Element
import javax.lang.model.type.TypeKind
import kotlin.reflect.KClass

fun Element.getType(): KClass<*> {
    val type = this.asType()
    return when (type.kind) {
        TypeKind.DECLARED -> {
            //When we have declared type check if it's a collection type and return dummy class
            //Not the best way to do it but first thing came to mind
            when {
                type.toString() =="java.lang.String" -> Class.forName(type.toString()).kotlin
                type.toString().contains("<") -> {
                    CollectionType()::class
                }
                else -> {
                    GeneratedType()::class
                }
            }
        }
        TypeKind.BOOLEAN -> Boolean::class
        TypeKind.BYTE -> Byte::class
        TypeKind.SHORT -> Short::class
        TypeKind.INT -> Int::class
        TypeKind.LONG -> Long::class
        TypeKind.CHAR -> Char::class
        TypeKind.FLOAT -> Float::class
        TypeKind.DOUBLE -> Double::class
        else -> throw Exception("Unknown type: $type, kind: ${type.kind}")
    }
}

fun Element.hasDeclaredTypes(): Boolean {
    return  this.enclosedElements.firstOrNull{  element ->
        //String is as a declared type by so should be excluded
        element.asType().kind == TypeKind.DECLARED && element.asType().toString() !="java.lang.String" } != null
}

class CollectionType
class GeneratedType


