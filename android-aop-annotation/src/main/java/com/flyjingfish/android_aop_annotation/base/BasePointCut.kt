package com.flyjingfish.android_aop_annotation.base

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint

/**
 * 注解切面的回调接口与 [com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut] 配合使用
 */
interface BasePointCut<T : Annotation> {
    /**
     * 使用自定义注解的方法被调用时回调这个方法
     * @param joinPoint 切点相关信息
     * @param anno 切点设置的注解
     */
    fun invoke(joinPoint: ProceedJoinPoint, anno: T): Any?
}