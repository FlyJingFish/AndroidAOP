package com.flyjingfish.android_aop_annotation.aop_anno



/**
 * 这个注解使用本库者用不到
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class AopPointCut(val value: String, val pointCutClassName: String)
