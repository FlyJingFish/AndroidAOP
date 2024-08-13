package com.flyjingfish.android_aop_annotation.anno

/**
 * 定义替换类的构造方法调用的注解，此注解用在替换类的
 * [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopReplaceConstructor)
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
annotation class AndroidAopReplaceNew(
    val value: String
)
