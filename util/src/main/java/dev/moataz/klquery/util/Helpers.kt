package dev.moataz.klquery.util

infix fun <B> String.to(that: B): Triple<String,Matchers, B> {
    return Triple(this, Matchers.TO, that)
}
infix fun <B> String.customTo(that: B): Triple<String,Matchers, B> {
    return Triple(this, Matchers.CUSTOM, that)
}
infix fun <B> String.match(that: B): Triple<String,Matchers, B> {
    return Triple(this, Matchers.MATCH, that)
}
infix fun <B> String.eq(that: B): Triple<String,Matchers, B> {
    return Triple(this, Matchers.EQ, that)
}
infix fun String.fromTo(that: KQLRange): Triple<String,Matchers, KQLRange> {
    return Triple(this, Matchers.FROMTO, that)
}
infix fun <B> String.`in`(that: List<B>): Triple<String,Matchers, List<B>> {
    return  Triple(this, Matchers.IN, that)
}

fun String.setFirsCharSmall(): String{
    return Character.toLowerCase(this[0]) + this.substring(1);
}