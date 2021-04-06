package dev.moataz.klquery.sample

import dev.moataz.klquery.annotation.KLQuery

@KLQuery
data class Result(
    val id: String,
    val name: String
)