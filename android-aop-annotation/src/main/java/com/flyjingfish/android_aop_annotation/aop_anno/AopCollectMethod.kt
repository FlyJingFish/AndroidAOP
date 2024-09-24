package com.flyjingfish.android_aop_annotation.aop_anno

import kotlin.reflect.KClass


/**
 * 这个注解使用本库者用不到
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class AopCollectMethod(
    /**
     * 需要收集的类名（包含包名）
     */
    val collectClass: KClass<*>,
    /**
     * 执行类名（包含包名）
     */
    val invokeClass: KClass<*>,
    /**
     * 执行方法
     */
    val invokeMethod: String,
    /**
     * 是否是 class
     */
    val isClazz:Boolean = false,
    /**
     * 正则表达式
     */
    val regex : String = "",
    /**
     * 收集的类型
     */
    val collectType : String
)
