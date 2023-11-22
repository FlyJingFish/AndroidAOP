package com.flyjingfish.android_aop_core.annotations;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut;
import com.flyjingfish.android_aop_core.cut.MainThreadCut;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 切换到主线程的操作，加入此注解可使你的方法内的代码切换到主线程执行
 */
@AndroidAopPointCut(MainThreadCut.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MainThread {
}
