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

    @Nullable
    public Object proceed(@Nullable OnSuspendReturnListener onSuspendReturnListener) {
        return super.proceed(onSuspendReturnListener,args);
    }

    @Nullable
    public Object proceed(@Nullable OnSuspendReturnListener onSuspendReturnListener,Object... args) {
        return super.proceed(onSuspendReturnListener, args);
    }

}
