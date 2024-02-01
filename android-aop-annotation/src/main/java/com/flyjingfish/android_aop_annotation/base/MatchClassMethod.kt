package com.flyjingfish.android_aop_annotation.base

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod

/**
 * 匹配切面的回调接口与 [AndroidAopMatchClassMethod] 配合使用
 */
interface MatchClassMethod {
    /**
     * 匹配到的方法被调用时将会回调这个方法
     * @param joinPoint 切点相关信息
     * @param methodName 匹配的方法名，如果是 Lambda 表达式，请看 wiki 文档
     * @return [wiki 文档返回值说明](https://github.com/FlyJingFish/AndroidAOP/wiki/%E5%88%87%E7%82%B9%E6%96%B9%E6%B3%95%E8%BF%94%E5%9B%9E%E5%80%BC)
     */
    fun invoke(joinPoint: ProceedJoinPoint, methodName:String): Any?
}