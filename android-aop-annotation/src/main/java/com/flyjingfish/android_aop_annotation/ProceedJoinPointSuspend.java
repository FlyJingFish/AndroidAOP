package com.flyjingfish.android_aop_annotation;

import com.flyjingfish.android_aop_annotation.base.OnSuspendReturnListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 切点相关信息类，<a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint">wiki 文档使用说明</a>
 */
public final class ProceedJoinPointSuspend extends ProceedJoinPoint{
    ProceedJoinPointSuspend(@NotNull Class<?> targetClass, Object[] args, @Nullable Object target, boolean isSuspend) {
        super(targetClass, args, target, isSuspend);
    }

    /**
     * 调用切点方法内代码，通过设置 {@link OnSuspendReturnListener} 可以修改返回值
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @return 返回切点方法返回值 <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed">wiki 文档使用说明</a>
     */
    @Nullable
    public Object proceed(@Nullable OnSuspendReturnListener onSuspendReturnListener) {
        return proceed(onSuspendReturnListener,args);
    }

    /**
     * 调用切点方法内代码，通过设置 {@link OnSuspendReturnListener} 可以修改返回值
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @param args 切点方法参数数组
     * @return 返回切点方法返回值 <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed">wiki 文档使用说明</a>
     */
    @Nullable
    public Object proceed(@Nullable OnSuspendReturnListener onSuspendReturnListener,Object... args) {
        return super.proceed(onSuspendReturnListener, args);
    }

    /**
     * 调用此方法则直接进入执行切点方法代码的阶段，忽略接下来的切面处理类
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @return 返回切点方法返回值 <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed">wiki 文档使用说明</a>
     */
    @Nullable
    public Object proceedIgnoreOther(@Nullable OnSuspendReturnListener onSuspendReturnListener) {
        return proceedIgnoreOther(onSuspendReturnListener,args);
    }

    /**
     * 调用此方法则直接进入执行切点方法代码的阶段，忽略接下来的切面处理类
     *
     * @param onSuspendReturnListener 设置 suspend 的函数的 返回前的监听，在此可修改返回值
     * @param args 切点方法参数数组
     * @return 返回切点方法返回值 <a href = "https://github.com/FlyJingFish/AndroidAOP/wiki/ProceedJoinPoint#proceed">wiki 文档使用说明</a>
     */
    @Nullable
    public Object proceedIgnoreOther(@Nullable OnSuspendReturnListener onSuspendReturnListener,Object... args) {
        setHasNext(false);
        return super.proceed(onSuspendReturnListener, args);
    }

}
