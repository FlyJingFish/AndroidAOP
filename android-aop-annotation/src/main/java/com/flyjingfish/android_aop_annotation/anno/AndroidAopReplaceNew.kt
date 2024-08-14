package com.flyjingfish.android_aop_annotation.anno

/**
 * 定义替换类的构造方法调用的切面的注解，使用这个注解的方法的类需要使用 [AndroidAopReplaceClass] 注解，否则无用
 * [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopReplaceClass)
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.BINARY)
annotation class AndroidAopReplaceNew
