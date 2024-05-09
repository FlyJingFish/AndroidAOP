package com.flyjingfish.android_aop_annotation.anno

/**
 * 定义收集类
 * [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopCollectMethod)
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
annotation class AndroidAopCollectMethod
