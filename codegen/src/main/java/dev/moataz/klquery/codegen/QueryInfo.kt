package dev.moataz.klquery.codegen

import com.squareup.kotlinpoet.LambdaTypeName
import javax.lang.model.element.Element

data class QueryInfo(
    val queryClassElement: Element,
    val queryPackage: String,
    val queryClassName: String
)