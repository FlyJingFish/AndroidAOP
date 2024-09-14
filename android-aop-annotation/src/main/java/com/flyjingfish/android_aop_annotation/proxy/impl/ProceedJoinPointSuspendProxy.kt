package com.flyjingfish.android_aop_annotation.proxy.impl

import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod
import com.flyjingfish.android_aop_annotation.anno.AndroidAopReplaceClass
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener2

/**
 *
 * 代理 [ProceedJoinPointProxy]，并使 [AndroidAopReplaceClass] 使用起来类似于 [AndroidAopMatchClassMethod]
 */
internal class ProceedJoinPointSuspendProxy(private val joinPoint: ProceedJoinPointSuspend): ProceedJoinPointProxy(joinPoint),ProceedJoinPointSuspend {
    override fun proceed(onSuspendReturnListener: OnSuspendReturnListener): Any? {
        return joinPoint.proceed(onSuspendReturnListener)
    }

    override fun proceed(
        onSuspendReturnListener: OnSuspendReturnListener,
        vararg args: Any?
    ): Any? {
        return joinPoint.proceed(onSuspendReturnListener,*args)
    }

    override fun proceedIgnoreOther(onSuspendReturnListener: OnSuspendReturnListener2): Any? {
        return joinPoint.proceedIgnoreOther(onSuspendReturnListener)
    }

    override fun proceedIgnoreOther(
        onSuspendReturnListener: OnSuspendReturnListener2,
        vararg args: Any?
    ): Any? {
        return joinPoint.proceedIgnoreOther(onSuspendReturnListener,*args)
    }
}