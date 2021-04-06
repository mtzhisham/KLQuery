package dev.moataz.klquery.util

sealed class Matchers{
    object TO : Matchers()
    object MATCH : Matchers()
    object EQ : Matchers()
    object FROMTO : Matchers()
    object IN : Matchers()
    object CUSTOM : Matchers()
}

