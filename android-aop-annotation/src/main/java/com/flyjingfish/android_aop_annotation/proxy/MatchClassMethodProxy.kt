package com.flyjingfish.android_aop_annotation.proxy

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod
import com.flyjingfish.android_aop_annotation.proxy.impl.ProceedJoinPointProxy


/**
 *
 * 代理 [MatchClassMethod]，并使 [AndroidAopReplaceClass] 使用起来类似于 [AndroidAopMatchClassMethod]
 *
 * 使用这个必须使用[ProxyMethod]来标记 [AndroidAopReplaceClass] 中的替换方法
 *
 * [wiki 文档使用说明](https://flyjingfish.github.io/AndroidAOP/zh/AndroidAopReplaceClass)
 */
abstract class MatchClassMethodProxy:MatchClassMethod {

    override fun invoke(joinPoint: ProceedJoinPoint, methodName:String): Any?{
        return invokeProxy(ProceedJoinPointProxy(joinPoint),methodName)
    }

    abstract fun invokeProxy(joinPoint: ProceedJoinPoint, methodName:String): Any?
}