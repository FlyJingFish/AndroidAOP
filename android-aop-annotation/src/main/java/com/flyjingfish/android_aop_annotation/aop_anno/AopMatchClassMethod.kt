package com.flyjingfish.android_aop_annotation.aop_anno



/**
 * 这个注解使用本库者用不到
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class AopMatchClassMethod(
    val baseClassName: String,
    val methodNames: String,
    val pointCutClassName: String,
    val matchType: String,
    val excludeClasses: String,
    val overrideMethod: String = "false"
)