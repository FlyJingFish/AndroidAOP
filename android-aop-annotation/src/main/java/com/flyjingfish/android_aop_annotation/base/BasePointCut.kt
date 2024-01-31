package com.flyjingfish.android_aop_annotation.base

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut

/**
 * 注解切面的回调接口与 [AndroidAopPointCut] 配合使用
 */
interface BasePointCut<T : Annotation> {
    /**
     * 使用自定义注解的方法被调用时回调这个方法
     * @param joinPoint 切点相关信息
     * @param anno 切点设置的注解
     * @return [wiki 文档返回值说明](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%88%87%E7%82%B9%E6%96%B9%E6%B3%95%E8%BF%94%E5%9B%9E%E5%80%BC)
     */
    fun invoke(joinPoint: ProceedJoinPoint, anno: T): Any?
}