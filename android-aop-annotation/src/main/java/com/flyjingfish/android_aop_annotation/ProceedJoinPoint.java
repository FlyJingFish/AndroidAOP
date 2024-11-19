package com.flyjingfish.android_aop_annotation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 切点相关信息类，<a href = "https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/">wiki 文档使用说明</a>
 */
public interface ProceedJoinPoint {
    /**
     *
     * <a href = "https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/#getargs">wiki 文档使用说明</a>
     *
     * @return 调用切点方法时传入的参数数组
     */
    @Nullable
    Object[] getArgs();

    /**
     * 调用切点方法内代码
     *
     * @return 返回切点方法返回值 <a href = "https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/#proceed">wiki 文档使用说明</a>
     * @throws Throwable 方法执行过程中可能发生错误
     */
    @Nullable
    Object proceed() throws Throwable;

    /**
     * 调用切点方法内代码
     *
     * @param args 切点方法参数数组
     * @return 返回切点方法返回值 <a href = "https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/#proceed">wiki 文档使用说明</a>
     * @throws Throwable 方法执行过程中可能发生错误
     */
    @Nullable
    Object proceed(Object... args) throws Throwable;

    /**
     * @return 切点方法相关信息，例如方法参数类型、返回类型、参数的注解等等
     */
    @NonNull
    AopMethod getTargetMethod();

    /**
     * @return 切点方法所在对象，如果方法为静态的，此值为null
     */
    @Nullable
    Object getTarget();

    /**
     * @return 切点方法所在类 Class
     */
    @NonNull
    Class<?> getTargetClass();

    /**
     * 和 {@link ProceedJoinPoint#getArgs()} 相比，返回的引用地址不同，但数组里边的对象一致。多用于多个切面逻辑时在某一个切面中获得最初的传入参数
     *
     * @return 最开始进入方法时的参数  <a href = "https://flyjingfish.github.io/AndroidAOP/zh/ProceedJoinPoint/#getargs">wiki 文档使用说明</a>
     */
    @Nullable
    Object[] getOriginalArgs();


}
