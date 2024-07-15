package com.flyjingfish.android_aop_annotation

import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener

class ProceedJoinPointSuspend(
    targetClass: Class<*>,
    args: Array<Any?>?,
    target: Any?,
    isSuspend: Boolean
) : ProceedJoinPoint(targetClass, args, target, isSuspend) {

    /**
     * 调用切点方法内代码，通过设置 [OnSuspendReturnListener] 可以修改返回值
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed)
     */
    fun proceed(onSuspendReturnListener: OnSuspendReturnListener): Any? {
        return proceed(onSuspendReturnListener, *args)
    }

    /**
     * 调用切点方法内代码，通过设置 [OnSuspendReturnListener] 可以修改返回值
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @param args 切点方法参数数组
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed)
     */
    override fun proceed(onSuspendReturnListener: OnSuspendReturnListener, vararg args: Any?): Any? {
        return super.proceed(onSuspendReturnListener, *args)
    }

    /**
     * 调用此方法则直接进入执行切点方法代码的阶段，忽略接下来的切面处理类
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed)
     */
    fun proceedIgnoreOther(onSuspendReturnListener: OnSuspendReturnListener): Any? {
        return proceedIgnoreOther(onSuspendReturnListener, *args)
    }

    /**
     * 调用此方法则直接进入执行切点方法代码的阶段，忽略接下来的切面处理类
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @param args 切点方法参数数组
     * @return 返回切点方法返回值 [wiki 文档使用说明](https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed)
     */
    fun proceedIgnoreOther(
        onSuspendReturnListener: OnSuspendReturnListener,
        vararg args: Any?
    ): Any? {
        setHasNext(false)
        return super.proceed(onSuspendReturnListener, *args)
    }

}