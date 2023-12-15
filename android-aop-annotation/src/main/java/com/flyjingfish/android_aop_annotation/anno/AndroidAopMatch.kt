package com.flyjingfish.android_aop_annotation.anno



/**
 * 这个注解使用本库者用不到
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class AndroidAopMatch(
    val baseClassName: String,
    val methodNames: String,
    val pointCutClassName: String,
    val matchType: String,
    val excludeClasses: String
)