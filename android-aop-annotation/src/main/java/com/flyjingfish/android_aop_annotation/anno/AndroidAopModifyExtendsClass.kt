package com.flyjingfish.android_aop_annotation.anno

/**
 * 定义替换类的继承类，并且这个类也是替换的类
 * [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopModifyExtendsClass)
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AndroidAopModifyExtendsClass(
    /**
     * @return 目标类名（包含包名）
     */
    val value: String,
)
