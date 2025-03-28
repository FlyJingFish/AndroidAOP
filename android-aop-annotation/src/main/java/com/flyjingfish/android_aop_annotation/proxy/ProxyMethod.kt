package com.flyjingfish.android_aop_annotation.proxy

import kotlin.reflect.KClass

import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass

/**
 * 将此注解加在 [AndroidAopReplaceClass] 所在类的替换方法上
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProxyMethod(
    /**
     *
     * 代理的实际目标类型，为适配作准备
     */
    val proxyClass: KClass<*>,
    /**
     * 代理的方法的实际类型，为适配作准备
     * 除了设置 [ProxyType.METHOD] ，其他类型暂时可以不作适配
     */
    val type: ProxyType
)
