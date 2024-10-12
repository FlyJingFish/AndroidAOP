package com.flyjingfish.android_aop_annotation

import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener
import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener2

interface ProceedJoinPointSuspend : ProceedJoinPoint {

    /**
     * 调用切点方法内代码，通过设置 [OnSuspendReturnListener] 可以修改返回值
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/#proceed)
     */
    fun proceed(onSuspendReturnListener: OnSuspendReturnListener): Any?

    /**
     * 调用切点方法内代码，通过设置 [OnSuspendReturnListener] 可以修改返回值
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @param args 切点方法参数数组
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/#proceed)
     */
    fun proceed(onSuspendReturnListener: OnSuspendReturnListener, vararg args: Any?): Any?

    /**
     * 调用此方法则直接进入执行切点方法代码的阶段，忽略接下来的切面处理类
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/#proceed)
     */
    fun proceedIgnoreOther(onSuspendReturnListener: OnSuspendReturnListener2): Any?

    /**
     * 调用此方法则直接进入执行切点方法代码的阶段，忽略接下来的切面处理类
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @param args 切点方法参数数组
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/#proceed)
     */
    fun proceedIgnoreOther(
        onSuspendReturnListener: OnSuspendReturnListener2,
        vararg args: Any?
    ): Any?

}