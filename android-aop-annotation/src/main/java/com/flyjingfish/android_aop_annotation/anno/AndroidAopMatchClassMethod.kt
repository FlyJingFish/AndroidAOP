package com.flyjingfish.android_aop_annotation.anno

import com.flyjingfish.android_aop_annotation.enums.MatchType


/**
 * 定义匹配类及方法切面的注解，使用这个注解的类需要实现 MatchClassMethod 接口，并且这个类也是处理切面的类
 * wiki 文档相关说明
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class AndroidAopMatchClassMethod(
    /**
     * @return 目标类名（包含包名）
     */
    val targetClassName: String,
    /**
     * @return 目标类中的方法名数组
     */
    val methodName: Array<String>,
    /**
     *
     * @return 返回匹配类型 [MatchType]
     */
    val type: MatchType = MatchType.EXTENDS,
    /**
     *
     * @return 排除继承中的类名数组（包含包名）
     */
    val excludeClasses: Array<String> = []
)