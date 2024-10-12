package com.flyjingfish.android_aop_annotation.proxy

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.base.MatchClassMethodSuspend
import com.flyjingfish.android_aop_annotation.proxy.impl.ProceedJoinPointProxy
import com.flyjingfish.android_aop_annotation.proxy.impl.ProceedJoinPointSuspendProxy

/**
 * 代理 [MatchClassMethodSuspend]，并使 [AndroidAopReplaceClass] 使用起来类似于 [AndroidAopMatchClassMethod]
 *
 * 使用这个必须使用[ProxyMethod]来标记 [AndroidAopReplaceClass] 中的替换方法
 *
 * [wiki 文档使用说明](https://flyjingfish.github.io/AndroidAOP/zh/AndroidAopReplaceClass)
 */
abstract class MatchClassMethodSuspendProxy : MatchClassMethodSuspend {
    override suspend fun invokeSuspend(joinPoint: ProceedJoinPointSuspend, methodName: String){
        invokeSuspendProxy(ProceedJoinPointSuspendProxy(joinPoint),methodName)
    }

    override fun invoke(joinPoint: ProceedJoinPoint, methodName: String): Any? {
        return invokeProxy(ProceedJoinPointProxy(joinPoint),methodName)
    }

    abstract fun invokeProxy(joinPoint: ProceedJoinPoint, methodName:String): Any?
    abstract suspend fun invokeSuspendProxy(joinPoint: ProceedJoinPointSuspend, methodName:String)
}