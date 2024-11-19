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
     * @return [wiki 文档返回值说明](https://flyjingfish.github.io/AndroidAOP/zh/Pointcut_return/)
     */
    @Throws(Throwable::class)
    fun invoke(joinPoint: ProceedJoinPoint, methodName:String): Any?
}