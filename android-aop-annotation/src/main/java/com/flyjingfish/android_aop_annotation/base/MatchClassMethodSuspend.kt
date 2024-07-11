package com.flyjingfish.android_aop_annotation.base

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod

/**
 * 匹配切面的回调接口与 [AndroidAopMatchClassMethod] 配合使用，与 [MatchClassMethod] 不同的是这个类支持 suspend
 * 要求切点函数必须是 suspend 修饰的才会回调 [invokeSuspend]，否则还是回调 [invoke]
 */
interface MatchClassMethodSuspend : MatchClassMethod {
    /**
     * 匹配到的被 suspend 修饰的方法被调用时将会回调这个方法,不会回调 [invoke]
     * @param joinPoint 切点相关信息
     * @param methodName 匹配的方法名，如果是 Lambda 表达式，请看 wiki 文档
     */
    suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, methodName: String)

    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        return joinPoint.proceed()
    }
}