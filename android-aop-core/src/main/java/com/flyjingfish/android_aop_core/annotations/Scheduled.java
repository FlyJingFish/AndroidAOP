package com.flyjingfish.android_aop_core.annotations;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut;
import com.flyjingfish.android_aop_core.cut.ScheduledCut;
import com.flyjingfish.android_aop_core.utils.AndroidAop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AndroidAopPointCut(ScheduledCut.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Scheduled {
    /**
     *
     * @return 任务 key ,可通过调用 {@link AndroidAop#shutdown(String)}
     * 或 {@link AndroidAop#shutdownNow(String)}停止任务,注意不要和其他 id 重合，包括 {@link Delay#id()}
     *
     */
    String id() default "";
    /**
     * @return 延迟多久开始,单位毫秒
     */
    long initialDelay() default 0L;
    /**
     *
     * @return 时间间隔,单位毫秒
     */
    long interval();

    /**
     * {@link Scheduled#repeatCount} 不限次数
     */
    int INFINITE = -1;

    /**
     * @return 执行次数,默认 {@link Scheduled#INFINITE}
     */
    int repeatCount() default INFINITE;

    /**
     *
     * @return 是否在主线程执行，默认主线程
     */
    boolean isOnMainThread() default true;
}
