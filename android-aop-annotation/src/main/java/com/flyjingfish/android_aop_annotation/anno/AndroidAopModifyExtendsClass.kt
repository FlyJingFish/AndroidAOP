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
    /**
     * @return [value]是否指向继承类
     */
    val isParent: Boolean = false,
    /**
     *
     * @return 排除织入的范围，类似于 [入门处的配置](https://flyjingfish.github.io/AndroidAOP/zh/getting_started/#app-buildgradle-androidaopconfig) 的 exclude
     */
    val excludeWeaving: Array<String> = [],
    /**
     *
     * @return 包括织入的范围，类似于 [入门处的配置](https://flyjingfish.github.io/AndroidAOP/zh/getting_started/#app-buildgradle-androidaopconfig) 的 include
     */
    val includeWeaving: Array<String> = []
)
