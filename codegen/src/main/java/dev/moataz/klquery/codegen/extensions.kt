package dev.moataz.klquery.codegen

import com.squareup.kotlinpoet.ClassName

fun ClassName.isPrimitive(): Boolean{
    return when (this.simpleName) {
        "Integer","String","Boolean", "Byte", "Short", "Int", "Long", "Char", "Float", "Double",
        "integer","string","boolean", "byte", "short", "int", "long", "char", "float", "double"-> true
        else -> false
    }
}