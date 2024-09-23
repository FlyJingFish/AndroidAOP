package com.flyjingfish.android_aop_annotation.anno

import com.flyjingfish.android_aop_annotation.enums.CollectType

/**
 * 定义收集类切面，如果想收集对象，则收集的对象不会是抽象类或接口，如果想收集 Class 则无此限制
 * [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/@AndroidAopCollectMethod)
 */
@Target(
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.SOURCE)
annotation class AndroidAopCollectMethod(
    /**
     * 收集的类型
     */
    val collectType : CollectType = CollectType.DIRECT_EXTENDS,
    /**
     * 当 [regex] 设置了正则表达式之后，注解方法的参数可以是 Object 或 Any ，不设置则必须指定类型
     */
    val regex : String = ""
)
