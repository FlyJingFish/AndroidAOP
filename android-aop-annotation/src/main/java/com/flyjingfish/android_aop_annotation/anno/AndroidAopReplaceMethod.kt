package com.flyjingfish.android_aop_annotation.anno

/**
 * 定义替换类的方法调用的切面的注解，使用这个注解的方法的类需要使用 [AndroidAopReplaceClass] 注解
 * wiki 文档相关说明
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
annotation class AndroidAopReplaceMethod(
    val value: String
)
