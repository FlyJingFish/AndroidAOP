package com.flyjingfish.android_aop_core.annotations;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut;
import com.flyjingfish.android_aop_core.cut.DelayCut;
import com.flyjingfish.android_aop_core.utils.AndroidAop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AndroidAopPointCut(DelayCut.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Delay {
    /**
     *
     * @return 任务 key ,可通过调用 {@link AndroidAop#shutdown(String)}
     * 或 {@link AndroidAop#shutdownNow(String)}停止任务,注意不要和其他 id 重合，包括 {@link Scheduled#id()}
     *
     */
    String id() default "";

    /**
     *
     * @return 延迟多久开始执行方法，单位是毫秒
     */
    long delay();
    /**
     *
     * @return 是否在主线程执行，默认主线程
     */
    boolean isOnMainThread() default true;


}