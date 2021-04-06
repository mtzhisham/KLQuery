package dev.moataz.klquery.sample

import dev.moataz.klquery.annotation.KLQuery

@KLQuery
data class Characters(
    val results: List<Result>
)