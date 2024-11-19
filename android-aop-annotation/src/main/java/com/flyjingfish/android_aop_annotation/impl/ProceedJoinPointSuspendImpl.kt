package com.flyjingfish.android_aop_annotation.impl

import com.flyjingfish.android_aop_annotation.AopMethod
import com.flyjingfish.android_aop_annotation.ProceedJoinPointSuspend
import com.flyjingfish.android_aop_annotation.base.OnBaseSuspendReturnListener
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener2
import com.flyjingfish.android_aop_annotation.utils.AndroidAopBeanUtils
import com.flyjingfish.android_aop_annotation.utils.InvokeMethod
import java.lang.reflect.Method

internal class ProceedJoinPointSuspendImpl(
    targetClass: Class<*>,
    args: Array<Any?>?,
    target: Any?,
    isSuspend: Boolean,
    targetMethod: Method,
    invokeMethod: InvokeMethod?,
    aopMethod: AopMethod
) : ProceedJoinPointImpl(targetClass, args, target, isSuspend,targetMethod, invokeMethod, aopMethod),
    ProceedJoinPointSuspend {

    /**
     * 调用切点方法内代码，通过设置 [OnSuspendReturnListener] 可以修改返回值
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/#proceed)
     */
    @Throws(Throwable::class)
    override fun proceed(onSuspendReturnListener: OnSuspendReturnListener): Any? {
        return super.realProceed(onSuspendReturnListener, *args)
    }

    /**
     * 调用切点方法内代码，通过设置 [OnSuspendReturnListener] 可以修改返回值
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @param args 切点方法参数数组
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/#proceed)
     */
    @Throws(Throwable::class)
    override fun proceed(onSuspendReturnListener: OnSuspendReturnListener, vararg args: Any?): Any? {
        return super.realProceed(onSuspendReturnListener, *args)
    }

    /**
     * 调用此方法则直接进入执行切点方法代码的阶段，忽略接下来的切面处理类
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/#proceed)
     */
    @Throws(Throwable::class)
    override fun proceedIgnoreOther(onSuspendReturnListener: OnSuspendReturnListener2): Any? {
        setExt(onSuspendReturnListener)
        return super.realProceed(onSuspendReturnListener, *args)
    }

    /**
     * 调用此方法则直接进入执行切点方法代码的阶段，忽略接下来的切面处理类
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @param args 切点方法参数数组
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/#proceed)
     */
    @Throws(Throwable::class)
    override fun proceedIgnoreOther(
        onSuspendReturnListener: OnSuspendReturnListener2,
        vararg args: Any?
    ): Any? {
        setExt(onSuspendReturnListener)
        return super.realProceed(onSuspendReturnListener, *args)
    }

    private fun setExt(onSuspendReturnListener: OnBaseSuspendReturnListener){
        setHasNext(false)
        AndroidAopBeanUtils.setIgnoreOther(onSuspendReturnListener)
    }
}