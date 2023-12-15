package com.flyjingfish.android_aop_core.annotations

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut
import com.flyjingfish.android_aop_core.cut.PermissionCut
import com.flyjingfish.android_aop_core.listeners.OnPermissionsInterceptListener

/**
 * 申请权限的操作，加入此注解可使您的代码在获取权限后才执行
 */
@AndroidAopPointCut(PermissionCut::class)
@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
annotation class Permission(
    /**
     * 需要申请权限的集合
     */
    vararg val value: String,
    /**
     * 设置一个标记，例如在哪个地方请求了权限，在 [OnPermissionsInterceptListener.requestPermission]回调中可以拿到
     */
    val tag: String = ""
)