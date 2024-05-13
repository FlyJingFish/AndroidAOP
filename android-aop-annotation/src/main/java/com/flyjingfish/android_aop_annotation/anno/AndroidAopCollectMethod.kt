package com.flyjingfish.android_aop_annotation.anno

/**
 * 定义收集类切面
 * [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopCollectMethod)
 */
@Target(
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.BINARY)
annotation class AndroidAopCollectMethod(
    /**
     * 当 [regex] 设置了正则表达式之后，注解方法的参数可以是 Object 或 Any ，不设置则必须执行类型
     */
    val regex : String = ""
)
