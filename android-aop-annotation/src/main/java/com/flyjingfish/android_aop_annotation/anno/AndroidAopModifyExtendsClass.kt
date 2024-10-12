package com.flyjingfish.android_aop_annotation.anno

/**
 * 定义修改类的继承类，并且这个类也是修改的继承类
 * [wiki 文档使用说明](https://flyjingfish.github.io/AndroidAOP/zh/AndroidAopModifyExtendsClass)
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AndroidAopModifyExtendsClass(
    /**
     * @return 被修改类的目标类名（包含包名）
     */
    val value: String,
)
