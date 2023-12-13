package com.flyjingfish.android_aop_core.annotations;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut;
import com.flyjingfish.android_aop_core.cut.PermissionCut;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.flyjingfish.android_aop_core.listeners.OnPermissionsInterceptListener;
import com.flyjingfish.android_aop_core.listeners.OnRequestPermissionListener;

/**
 * 申请权限的操作，加入此注解可使您的代码在获取权限后才执行
 */
@AndroidAopPointCut(PermissionCut.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Permission {
    /**
     * @return 需要申请权限的集合
     */
    String[] value();

    /**
     * @return 设置一个标记，例如在哪个地方请求了权限，在 {@link OnPermissionsInterceptListener#requestPermission(ProceedJoinPoint, Permission, OnRequestPermissionListener)}回调中可以拿到
     */
    String tag() default "";
}