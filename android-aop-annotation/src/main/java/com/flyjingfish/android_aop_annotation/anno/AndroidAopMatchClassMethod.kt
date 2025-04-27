package com.flyjingfish.android_aop_annotation.anno

import com.flyjingfish.android_aop_annotation.enums.MatchType
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod


/**
 * 定义匹配类及方法切面的注解，使用这个注解的类需要实现 [MatchClassMethod] 接口，并且这个类也是处理切面的类
 *  [wiki 文档使用说明](https://flyjingfish.github.io/AndroidAOP/zh/AndroidAopMatchClassMethod)
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
    val excludeClasses: Array<String> = [],
    /**
     *
     * @return 如果子类中没有匹配的方法则重写父类的方法，targetClassName 不可以包含 * ，methodName 不可以定义 [ "*" ]，并且方法不能是private 、final修饰的才可以，重写所在类不可以是接口
     */
    val overrideMethod: Boolean = false,
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