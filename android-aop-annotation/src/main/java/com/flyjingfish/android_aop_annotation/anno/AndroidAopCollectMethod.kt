package com.flyjingfish.android_aop_annotation.anno

/**
 * 定义收集类切面
 * [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopCollectMethod)
 */
@Target(
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.BINARY)
annotation class AndroidAopCollectMethod
